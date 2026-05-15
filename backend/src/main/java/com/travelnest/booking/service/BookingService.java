package com.travelnest.booking.service;

import com.travelnest.booking.dto.BookingResponse;
import com.travelnest.booking.dto.BookingActionRequest;
import com.travelnest.booking.dto.CreateBookingRequest;
import com.travelnest.booking.entity.BookingEntity;
import com.travelnest.booking.entity.HotelBookingEntity;
import com.travelnest.booking.entity.RestaurantBookingEntity;
import com.travelnest.booking.entity.TourBookingEntity;
import com.travelnest.booking.repository.BookingRepository;
import com.travelnest.common.exception.BadRequestException;
import com.travelnest.common.exception.ResourceNotFoundException;
import com.travelnest.hotel.entity.HotelEntity;
import com.travelnest.hotel.entity.RoomTypeEntity;
import com.travelnest.hotel.repository.HotelRepository;
import com.travelnest.order.entity.OrderEntity;
import com.travelnest.order.entity.OrderItemEntity;
import com.travelnest.order.repository.OrderRepository;
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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final TourRepository tourRepository;
    private final RestaurantRepository restaurantRepository;

    public BookingService(
            BookingRepository bookingRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            HotelRepository hotelRepository,
            TourRepository tourRepository,
            RestaurantRepository restaurantRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.hotelRepository = hotelRepository;
        this.tourRepository = tourRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional
    public BookingResponse createBooking(AuthenticatedUser authenticatedUser, CreateBookingRequest request) {
        UserEntity user = requireUser(authenticatedUser.getUserId());
        OrderEntity order = orderRepository.findByIdAndUserId(request.getOrderId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        OrderItemEntity orderItem = getPrimaryOrderItem(order);

        if (bookingRepository.existsByOrderItemId(orderItem.getId())) {
            throw new BadRequestException("A booking already exists for this order");
        }

        BookingEntity booking = new BookingEntity();
        booking.setBookingCode(generateBookingCode());
        booking.setOrder(order);
        booking.setOrderItem(orderItem);
        booking.setUser(user);
        booking.setServiceType(orderItem.getServiceType());
        booking.setServiceId(orderItem.getServiceId());
        booking.setContactName(order.getContactName());
        booking.setContactPhone(order.getContactPhone());
        booking.setContactEmail(order.getContactEmail());
        booking.setGuestCount(orderItem.getGuestCount());
        booking.setSpecialRequests(order.getSpecialRequests());
        booking.setStatus("PENDING_CONFIRMATION");

        switch (normalizeServiceType(orderItem.getServiceType())) {
            case "HOTEL" -> booking.setHotelBooking(buildHotelBooking(orderItem));
            case "TOUR" -> booking.setTourBooking(buildTourBooking(orderItem));
            case "RESTAURANT" -> booking.setRestaurantBooking(buildRestaurantBooking(orderItem));
            default -> throw new BadRequestException("Unsupported service type");
        }

        order.setStatus("BOOKED");

        BookingEntity savedBooking = bookingRepository.save(booking);
        return mapResponse(savedBooking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getCurrentUserBookings(AuthenticatedUser authenticatedUser) {
        requireUser(authenticatedUser.getUserId());
        return bookingRepository.findAllByUserIdOrderByCreatedAtDesc(authenticatedUser.getUserId()).stream()
                .map(this::mapResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getManagementBookings(String status, String serviceType, LocalDate serviceDate) {
        return bookingRepository.searchManagementBookings(
                        normalizeFilter(status),
                        normalizeFilter(serviceType),
                        serviceDate
                ).stream()
                .map(this::mapResponse)
                .toList();
    }

    @Transactional
    public BookingResponse confirmBooking(
            AuthenticatedUser authenticatedUser,
            Long bookingId,
            BookingActionRequest request
    ) {
        UserEntity actor = requireUser(authenticatedUser.getUserId());
        BookingEntity booking = requireBooking(bookingId);
        assertStatus(booking, "PENDING_CONFIRMATION", "Only pending bookings can be confirmed");
        booking.setStatus("CONFIRMED");
        booking.setConfirmedAt(LocalDateTime.now());
        booking.setStaff(actor);
        applyStaffNote(booking, request);
        booking.getOrder().setStatus("CONFIRMED");
        return mapResponse(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(
            AuthenticatedUser authenticatedUser,
            Long bookingId,
            BookingActionRequest request
    ) {
        UserEntity actor = requireUser(authenticatedUser.getUserId());
        BookingEntity booking = requireBooking(bookingId);
        if (!"PENDING_CONFIRMATION".equals(booking.getStatus()) && !"CONFIRMED".equals(booking.getStatus())) {
            throw new BadRequestException("Only pending or confirmed bookings can be cancelled");
        }
        booking.setStatus("CANCELLED");
        booking.setCancelledAt(LocalDateTime.now());
        booking.setStaff(actor);
        applyStaffNote(booking, request);
        booking.setCancelReason(resolveCancelReason(request, actor));
        booking.getOrder().setStatus("CANCELLED");
        return mapResponse(booking);
    }

    @Transactional
    public BookingResponse completeBooking(
            AuthenticatedUser authenticatedUser,
            Long bookingId,
            BookingActionRequest request
    ) {
        UserEntity actor = requireUser(authenticatedUser.getUserId());
        BookingEntity booking = requireBooking(bookingId);
        assertStatus(booking, "CONFIRMED", "Only confirmed bookings can be completed");
        booking.setStatus("COMPLETED");
        booking.setCompletedAt(LocalDateTime.now());
        booking.setStaff(actor);
        applyStaffNote(booking, request);
        booking.getOrder().setStatus("COMPLETED");
        return mapResponse(booking);
    }

    private HotelBookingEntity buildHotelBooking(OrderItemEntity orderItem) {
        HotelEntity hotel = hotelRepository.findByIdAndDeletedFalseAndStatus(orderItem.getServiceId(), "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));
        LocalDate checkInDate = orderItem.getStartDate();
        LocalDate checkOutDate = orderItem.getEndDate();
        if (checkInDate == null || checkOutDate == null) {
            throw new BadRequestException("Hotel order is missing stay dates");
        }

        long nightCount = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (nightCount <= 0) {
            throw new BadRequestException("Hotel booking dates are invalid");
        }

        RoomTypeEntity roomType = findRoomTypeByName(hotel, orderItem.getVariantName()).orElse(null);
        int roomCount = Math.max(1, orderItem.getQuantity() / Math.max(1, (int) nightCount));

        HotelBookingEntity hotelBooking = new HotelBookingEntity();
        hotelBooking.setHotel(hotel);
        hotelBooking.setRoomType(roomType);
        hotelBooking.setCheckInDate(checkInDate);
        hotelBooking.setCheckOutDate(checkOutDate);
        hotelBooking.setNumNights((int) nightCount);
        hotelBooking.setNumRooms(roomCount);
        hotelBooking.setPricePerNight(orderItem.getUnitPrice());
        return hotelBooking;
    }

    private TourBookingEntity buildTourBooking(OrderItemEntity orderItem) {
        TourEntity tour = tourRepository.findByIdAndDeletedFalseAndStatus(orderItem.getServiceId(), "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found"));
        TourSlotEntity slot = tour.getSlots().stream()
                .filter(candidate -> orderItem.getVariantId() != null && orderItem.getVariantId().equals(candidate.getId()))
                .findFirst()
                .orElse(null);

        if (orderItem.getStartDate() == null || orderItem.getEndDate() == null) {
            throw new BadRequestException("Tour order is missing departure dates");
        }

        TourBookingEntity tourBooking = new TourBookingEntity();
        tourBooking.setTour(tour);
        tourBooking.setTourSlot(slot);
        tourBooking.setDepartureDate(orderItem.getStartDate());
        tourBooking.setEndDate(orderItem.getEndDate());
        tourBooking.setDepartureTime(orderItem.getServiceTime());
        tourBooking.setGuestCount(orderItem.getGuestCount());
        tourBooking.setPricePerGuest(orderItem.getUnitPrice());
        return tourBooking;
    }

    private RestaurantBookingEntity buildRestaurantBooking(OrderItemEntity orderItem) {
        RestaurantEntity restaurant = restaurantRepository.findByIdAndDeletedFalseAndStatus(orderItem.getServiceId(), "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        if (orderItem.getStartDate() == null || orderItem.getServiceTime() == null) {
            throw new BadRequestException("Restaurant order is missing reservation details");
        }

        RestaurantBookingEntity restaurantBooking = new RestaurantBookingEntity();
        restaurantBooking.setRestaurant(restaurant);
        restaurantBooking.setReservationDate(orderItem.getStartDate());
        restaurantBooking.setReservationTime(orderItem.getServiceTime());
        restaurantBooking.setPartySize(orderItem.getGuestCount());
        restaurantBooking.setPricePerGuest(orderItem.getUnitPrice());
        return restaurantBooking;
    }

    private BookingResponse mapResponse(BookingEntity booking) {
        OrderItemEntity orderItem = booking.getOrderItem();
        return new BookingResponse(
                booking.getId(),
                booking.getBookingCode(),
                booking.getOrder().getOrderCode(),
                booking.getStatus(),
                booking.getServiceType(),
                booking.getServiceId(),
                orderItem.getServiceName(),
                orderItem.getVariantId(),
                orderItem.getVariantName(),
                orderItem.getQuantity(),
                booking.getGuestCount(),
                orderItem.getStartDate(),
                orderItem.getEndDate(),
                orderItem.getServiceTime(),
                booking.getOrder().getTotalAmount(),
                booking.getOrder().getPreferredPaymentMethod(),
                booking.getContactName(),
                booking.getContactPhone(),
                booking.getContactEmail(),
                booking.getSpecialRequests(),
                booking.getCreatedAt(),
                booking.getStaff() == null ? null : firstNonBlank(booking.getStaff().getFullName(), booking.getStaff().getEmail()),
                booking.getStaffNote(),
                booking.getCancelReason(),
                booking.getConfirmedAt(),
                booking.getCancelledAt(),
                booking.getCompletedAt()
        );
    }

    private BookingEntity requireBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    private OrderItemEntity getPrimaryOrderItem(OrderEntity order) {
        return order.getItems().stream()
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Order is missing line items"));
    }

    private UserEntity requireUser(Long userId) {
        return userRepository.findById(userId)
                .filter(user -> !user.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void assertStatus(BookingEntity booking, String expectedStatus, String message) {
        if (!expectedStatus.equals(booking.getStatus())) {
            throw new BadRequestException(message);
        }
    }

    private void applyStaffNote(BookingEntity booking, BookingActionRequest request) {
        if (request == null || request.getStaffNote() == null || request.getStaffNote().isBlank()) {
            return;
        }
        booking.setStaffNote(request.getStaffNote().trim());
    }

    private String resolveCancelReason(BookingActionRequest request, UserEntity actor) {
        if (request != null && request.getCancelReason() != null && !request.getCancelReason().isBlank()) {
            return request.getCancelReason().trim();
        }
        return "Cancelled by " + firstNonBlank(actor.getFullName(), actor.getEmail());
    }

    private Optional<RoomTypeEntity> findRoomTypeByName(HotelEntity hotel, String variantName) {
        if (variantName == null || variantName.isBlank()) {
            return Optional.empty();
        }
        return hotel.getRoomTypes().stream()
                .filter(roomType -> !roomType.isDeleted())
                .filter(roomType -> variantName.equalsIgnoreCase(firstNonBlank(roomType.getNameEn(), roomType.getNameVi())))
                .min(Comparator.comparing(RoomTypeEntity::getBasePrice));
    }

    private String normalizeServiceType(String value) {
        String normalized = value == null ? null : value.trim().toUpperCase(Locale.ROOT);
        if (!"HOTEL".equals(normalized) && !"TOUR".equals(normalized) && !"RESTAURANT".equals(normalized)) {
            throw new BadRequestException("Service type must be HOTEL, TOUR, or RESTAURANT");
        }
        return normalized;
    }

    private String normalizeFilter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String generateBookingCode() {
        return "BK-" + String.format("%012d", ThreadLocalRandom.current().nextLong(1_000_000_000_000L));
    }

    private String firstNonBlank(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred.trim();
        }
        return fallback == null ? "" : fallback.trim();
    }
}
