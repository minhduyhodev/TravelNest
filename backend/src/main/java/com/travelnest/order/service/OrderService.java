package com.travelnest.order.service;

import com.travelnest.common.exception.BadRequestException;
import com.travelnest.common.exception.ResourceNotFoundException;
import com.travelnest.hotel.entity.HotelEntity;
import com.travelnest.hotel.entity.RoomTypeEntity;
import com.travelnest.hotel.repository.HotelRepository;
import com.travelnest.order.dto.CreateOrderRequest;
import com.travelnest.order.dto.OrderResponse;
import com.travelnest.order.entity.OrderEntity;
import com.travelnest.order.entity.OrderItemEntity;
import com.travelnest.order.repository.OrderRepository;
import com.travelnest.restaurant.entity.MenuItemEntity;
import com.travelnest.restaurant.entity.RestaurantEntity;
import com.travelnest.restaurant.repository.RestaurantRepository;
import com.travelnest.security.AuthenticatedUser;
import com.travelnest.tour.entity.TourEntity;
import com.travelnest.tour.entity.TourSlotEntity;
import com.travelnest.tour.repository.TourRepository;
import com.travelnest.user.entity.UserEntity;
import com.travelnest.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private static final DateTimeFormatter ORDER_CODE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter DATE_LABEL_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final TourRepository tourRepository;
    private final RestaurantRepository restaurantRepository;

    public OrderService(
            OrderRepository orderRepository,
            UserRepository userRepository,
            HotelRepository hotelRepository,
            TourRepository tourRepository,
            RestaurantRepository restaurantRepository
    ) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.hotelRepository = hotelRepository;
        this.tourRepository = tourRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional
    public OrderResponse createDraftOrder(AuthenticatedUser authenticatedUser, CreateOrderRequest request) {
        UserEntity user = requireUser(authenticatedUser.getUserId());
        String serviceType = normalizeServiceType(request.getServiceType());
        String paymentMethod = normalizePaymentMethod(request.getPaymentMethod());
        DraftOrderItemSnapshot snapshot = buildSnapshot(serviceType, request);

        OrderEntity order = new OrderEntity();
        order.setOrderCode(generateOrderCode());
        order.setUser(user);
        order.setSubtotal(snapshot.subtotal());
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setTotalAmount(snapshot.subtotal());
        order.setVoucherCode(trimToNull(request.getVoucherCode()));
        order.setPreferredPaymentMethod(paymentMethod);
        order.setContactName(request.getContactFullName().trim());
        order.setContactPhone(request.getContactPhone().trim());
        order.setContactEmail(request.getContactEmail().trim());
        order.setSpecialRequests(trimToNull(request.getSpecialRequests()));
        order.setStatus("PENDING");
        order.setLang(resolveLang(user));

        OrderItemEntity item = new OrderItemEntity();
        item.setServiceType(serviceType);
        item.setServiceId(request.getServiceId());
        item.setServiceName(snapshot.serviceName());
        item.setVariantId(snapshot.variantId());
        item.setVariantName(snapshot.variantName());
        item.setUnitPrice(snapshot.unitPrice());
        item.setQuantity(snapshot.quantity());
        item.setGuestCount(snapshot.guestCount());
        item.setStartDate(snapshot.startDate());
        item.setEndDate(snapshot.endDate());
        item.setServiceTime(snapshot.serviceTime());
        item.setSubtotal(snapshot.subtotal());
        order.addItem(item);

        OrderEntity savedOrder = orderRepository.save(order);
        return mapResponse(savedOrder, item);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getCurrentUserOrders(AuthenticatedUser authenticatedUser) {
        requireUser(authenticatedUser.getUserId());
        return orderRepository.findAllByUserIdOrderByCreatedAtDesc(authenticatedUser.getUserId()).stream()
                .filter(order -> !order.getItems().isEmpty())
                .map(order -> mapResponse(order, getPrimaryItem(order)))
                .toList();
    }

    private DraftOrderItemSnapshot buildSnapshot(String serviceType, CreateOrderRequest request) {
        return switch (serviceType) {
            case "HOTEL" -> buildHotelSnapshot(request);
            case "TOUR" -> buildTourSnapshot(request);
            case "RESTAURANT" -> buildRestaurantSnapshot(request);
            default -> throw new BadRequestException("Unsupported service type");
        };
    }

    private DraftOrderItemSnapshot buildHotelSnapshot(CreateOrderRequest request) {
        HotelEntity hotel = hotelRepository.findByIdAndDeletedFalseAndStatus(request.getServiceId(), "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        LocalDate checkInDate = request.getCheckInDate();
        LocalDate checkOutDate = request.getCheckOutDate();
        if (checkInDate == null || checkOutDate == null) {
            throw new BadRequestException("Hotel bookings require check-in and check-out dates");
        }

        long nightCount = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (nightCount <= 0) {
            throw new BadRequestException("Check-out date must be after check-in date");
        }

        int roomCount = requirePositive(request.getRoomCount(), "Room count must be at least 1");
        int guestCount = requirePositive(request.getGuestCount(), "Guest count must be at least 1");
        BigDecimal unitPrice = hotel.getRoomTypes().stream()
                .filter(roomType -> !roomType.isDeleted())
                .map(RoomTypeEntity::getBasePrice)
                .filter(price -> price != null)
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new BadRequestException("Hotel is missing a public stay price"));
        int quantity = Math.toIntExact(nightCount * roomCount);
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

        return new DraftOrderItemSnapshot(
                firstNonBlank(hotel.getNameEn(), hotel.getNameVi()),
                null,
                trimToNull(request.getRoomLabel()),
                unitPrice,
                quantity,
                guestCount,
                checkInDate,
                checkOutDate,
                null,
                subtotal
        );
    }

    private DraftOrderItemSnapshot buildTourSnapshot(CreateOrderRequest request) {
        TourEntity tour = tourRepository.findByIdAndDeletedFalseAndStatus(request.getServiceId(), "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found"));

        LocalDate departureDate = request.getDepartureDate();
        if (departureDate == null) {
            throw new BadRequestException("Tour bookings require a departure date");
        }

        int guestCount = requirePositive(request.getGuestCount(), "Traveler count must be at least 1");
        TourSlotEntity slot = tour.getSlots().stream()
                .filter(candidate -> "OPEN".equalsIgnoreCase(candidate.getStatus()))
                .filter(candidate -> departureDate.equals(candidate.getStartDate()))
                .min(Comparator.comparing(TourSlotEntity::getStartDate))
                .orElseThrow(() -> new BadRequestException("No open tour departure is available for the selected date"));
        BigDecimal unitPrice = slot.getPricePerPerson();
        if (unitPrice == null) {
            throw new BadRequestException("Tour slot is missing pricing");
        }

        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(guestCount));
        String variantName = "Departure " + slot.getStartDate().format(DATE_LABEL_FORMATTER);

        return new DraftOrderItemSnapshot(
                firstNonBlank(tour.getNameEn(), tour.getNameVi()),
                slot.getId(),
                variantName,
                unitPrice,
                guestCount,
                guestCount,
                slot.getStartDate(),
                slot.getEndDate(),
                slot.getDepartureTime(),
                subtotal
        );
    }

    private DraftOrderItemSnapshot buildRestaurantSnapshot(CreateOrderRequest request) {
        RestaurantEntity restaurant = restaurantRepository.findByIdAndDeletedFalseAndStatus(request.getServiceId(), "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        LocalDate reservationDate = request.getReservationDate();
        if (reservationDate == null || request.getReservationTime() == null) {
            throw new BadRequestException("Restaurant bookings require a reservation date and time");
        }

        if (restaurant.getOpenTime() != null
                && restaurant.getCloseTime() != null
                && (request.getReservationTime().isBefore(restaurant.getOpenTime())
                || request.getReservationTime().isAfter(restaurant.getCloseTime()))) {
            throw new BadRequestException("Reservation time must be within the restaurant operating hours");
        }

        int guestCount = requirePositive(request.getGuestCount(), "Party size must be at least 1");
        BigDecimal unitPrice = restaurant.getMenuItems().stream()
                .filter(item -> item.isAvailable() && !item.isDeleted())
                .map(MenuItemEntity::getPrice)
                .filter(price -> price != null)
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new BadRequestException("Restaurant is missing a public menu price"));
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(guestCount));

        return new DraftOrderItemSnapshot(
                firstNonBlank(restaurant.getNameEn(), restaurant.getNameVi()),
                null,
                "Table reservation",
                unitPrice,
                guestCount,
                guestCount,
                reservationDate,
                reservationDate,
                request.getReservationTime(),
                subtotal
        );
    }

    private OrderResponse mapResponse(OrderEntity order, OrderItemEntity item) {
        return new OrderResponse(
                order.getId(),
                order.getOrderCode(),
                order.getStatus(),
                item.getServiceType(),
                item.getServiceId(),
                item.getServiceName(),
                item.getVariantId(),
                item.getVariantName(),
                item.getQuantity(),
                item.getGuestCount(),
                item.getStartDate(),
                item.getEndDate(),
                item.getServiceTime(),
                item.getUnitPrice(),
                order.getSubtotal(),
                order.getDiscountAmount(),
                order.getTotalAmount(),
                order.getVoucherCode(),
                order.getPreferredPaymentMethod(),
                order.getContactName(),
                order.getContactPhone(),
                order.getContactEmail(),
                order.getSpecialRequests(),
                order.getCreatedAt()
        );
    }

    private OrderItemEntity getPrimaryItem(OrderEntity order) {
        return order.getItems().stream()
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Order is missing line items"));
    }

    private UserEntity requireUser(Long userId) {
        return userRepository.findById(userId)
                .filter(user -> !user.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String normalizeServiceType(String value) {
        String normalized = value == null ? null : value.trim().toUpperCase(Locale.ROOT);
        if (!"HOTEL".equals(normalized) && !"TOUR".equals(normalized) && !"RESTAURANT".equals(normalized)) {
            throw new BadRequestException("Service type must be HOTEL, TOUR, or RESTAURANT");
        }
        return normalized;
    }

    private String normalizePaymentMethod(String value) {
        String normalized = value == null ? null : value.trim().toUpperCase(Locale.ROOT);
        if (!"VNPAY".equals(normalized) && !"MOMO".equals(normalized)) {
            throw new BadRequestException("Payment method must be VNPAY or MOMO");
        }
        return normalized;
    }

    private int requirePositive(Integer value, String message) {
        if (value == null || value < 1) {
            throw new BadRequestException(message);
        }
        return value;
    }

    private String resolveLang(UserEntity user) {
        return user.getPreferredLang() == null || user.getPreferredLang().isBlank()
                ? "vi"
                : user.getPreferredLang().trim();
    }

    private String generateOrderCode() {
        return "TN-" + java.time.LocalDateTime.now().format(ORDER_CODE_DATE_FORMATTER)
                + "-" + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }

    private String firstNonBlank(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred.trim();
        }
        return fallback == null ? "" : fallback.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record DraftOrderItemSnapshot(
            String serviceName,
            Long variantId,
            String variantName,
            BigDecimal unitPrice,
            Integer quantity,
            Integer guestCount,
            LocalDate startDate,
            LocalDate endDate,
            java.time.LocalTime serviceTime,
            BigDecimal subtotal
    ) {
    }
}
