package com.travelnest.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.travelnest.booking.dto.BookingResponse;
import com.travelnest.booking.dto.CreateBookingRequest;
import com.travelnest.booking.entity.BookingEntity;
import com.travelnest.booking.repository.BookingRepository;
import com.travelnest.common.exception.BadRequestException;
import com.travelnest.hotel.entity.HotelEntity;
import com.travelnest.hotel.entity.RoomTypeEntity;
import com.travelnest.hotel.repository.HotelRepository;
import com.travelnest.order.entity.OrderEntity;
import com.travelnest.order.entity.OrderItemEntity;
import com.travelnest.order.repository.OrderRepository;
import com.travelnest.restaurant.repository.RestaurantRepository;
import com.travelnest.security.AuthenticatedUser;
import com.travelnest.tour.repository.TourRepository;
import com.travelnest.user.entity.UserEntity;
import com.travelnest.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private TourRepository tourRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    private BookingService bookingService;
    private AuthenticatedUser authenticatedUser;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(
                bookingRepository,
                orderRepository,
                userRepository,
                hotelRepository,
                tourRepository,
                restaurantRepository
        );
        authenticatedUser = new AuthenticatedUser(
                7L,
                "customer@travelnest.test",
                "secret",
                "Travel Customer",
                "CUSTOMER",
                Set.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        user = new UserEntity();
        user.setId(7L);
    }

    @Test
    void createBooking_createsHotelBookingAndMarksOrderBooked() {
        OrderEntity order = new OrderEntity();
        order.setId(1001L);
        order.setOrderCode("TN-1001");
        order.setUser(user);
        order.setContactName("Travel Customer");
        order.setContactPhone("0901234567");
        order.setContactEmail("customer@travelnest.test");
        order.setTotalAmount(new BigDecimal("14400000"));
        order.setPreferredPaymentMethod("VNPAY");
        order.setStatus("PENDING");

        OrderItemEntity item = new OrderItemEntity();
        item.setId(2001L);
        item.setServiceType("HOTEL");
        item.setServiceId(15L);
        item.setServiceName("Da Nang Ocean Suites");
        item.setVariantName("Family Suite");
        item.setUnitPrice(new BigDecimal("2400000"));
        item.setQuantity(6);
        item.setGuestCount(3);
        item.setStartDate(LocalDate.of(2026, 6, 10));
        item.setEndDate(LocalDate.of(2026, 6, 13));
        order.addItem(item);

        HotelEntity hotel = new HotelEntity();
        hotel.setId(15L);
        RoomTypeEntity roomType = new RoomTypeEntity();
        roomType.setNameEn("Family Suite");
        roomType.setBasePrice(new BigDecimal("3100000"));
        hotel.setRoomTypes(new LinkedHashSet<>(Set.of(roomType)));

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(orderRepository.findByIdAndUserId(1001L, 7L)).thenReturn(Optional.of(order));
        when(bookingRepository.existsByOrderItemId(2001L)).thenReturn(false);
        when(hotelRepository.findByIdAndDeletedFalseAndStatus(15L, "ACTIVE")).thenReturn(Optional.of(hotel));
        when(bookingRepository.save(any(BookingEntity.class))).thenAnswer(invocation -> {
            BookingEntity booking = invocation.getArgument(0);
            booking.setId(3001L);
            return booking;
        });

        BookingResponse response = bookingService.createBooking(authenticatedUser, new CreateBookingRequest(1001L));

        assertThat(response.getId()).isEqualTo(3001L);
        assertThat(response.getStatus()).isEqualTo("PENDING_CONFIRMATION");
        assertThat(response.getServiceType()).isEqualTo("HOTEL");
        assertThat(response.getOrderCode()).isEqualTo("TN-1001");
        assertThat(order.getStatus()).isEqualTo("BOOKED");
    }

    @Test
    void createBooking_rejectsDuplicateBookingForSameOrderItem() {
        OrderEntity order = new OrderEntity();
        order.setId(1001L);
        order.setUser(user);

        OrderItemEntity item = new OrderItemEntity();
        item.setId(2001L);
        item.setServiceType("HOTEL");
        order.addItem(item);

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(orderRepository.findByIdAndUserId(1001L, 7L)).thenReturn(Optional.of(order));
        when(bookingRepository.existsByOrderItemId(2001L)).thenReturn(true);

        assertThatThrownBy(() -> bookingService.createBooking(authenticatedUser, new CreateBookingRequest(1001L)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("A booking already exists for this order");
    }

    @Test
    void getCurrentUserBookings_returnsNewestFirst() {
        OrderEntity order = new OrderEntity();
        order.setOrderCode("TN-1001");
        order.setTotalAmount(new BigDecimal("8580000"));
        order.setPreferredPaymentMethod("MOMO");

        OrderItemEntity item = new OrderItemEntity();
        item.setServiceType("TOUR");
        item.setServiceId(8L);
        item.setServiceName("Ha Giang Loop Escape");
        item.setVariantId(21L);
        item.setVariantName("Departure 2026-06-08");
        item.setQuantity(2);
        item.setGuestCount(2);
        item.setStartDate(LocalDate.of(2026, 6, 8));
        item.setEndDate(LocalDate.of(2026, 6, 10));
        order.addItem(item);

        BookingEntity booking = new BookingEntity();
        booking.setId(3001L);
        booking.setBookingCode("BK-001");
        booking.setOrder(order);
        booking.setOrderItem(item);
        booking.setUser(user);
        booking.setServiceType("TOUR");
        booking.setServiceId(8L);
        booking.setStatus("PENDING_CONFIRMATION");
        booking.setContactName("Travel Customer");
        booking.setContactPhone("0901234567");
        booking.setContactEmail("customer@travelnest.test");
        booking.setGuestCount(2);

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByUserIdOrderByCreatedAtDesc(7L)).thenReturn(List.of(booking));

        List<BookingResponse> response = bookingService.getCurrentUserBookings(authenticatedUser);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().getBookingCode()).isEqualTo("BK-001");
        assertThat(response.getFirst().getServiceName()).isEqualTo("Ha Giang Loop Escape");
    }
}
