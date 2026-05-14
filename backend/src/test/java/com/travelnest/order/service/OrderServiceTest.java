package com.travelnest.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.travelnest.common.exception.BadRequestException;
import com.travelnest.hotel.entity.HotelEntity;
import com.travelnest.hotel.entity.RoomTypeEntity;
import com.travelnest.hotel.repository.HotelRepository;
import com.travelnest.order.dto.CreateOrderRequest;
import com.travelnest.order.dto.OrderResponse;
import com.travelnest.order.entity.OrderEntity;
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
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

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

    private OrderService orderService;
    private AuthenticatedUser authenticatedUser;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
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
        user.setPreferredLang("en");
    }

    @Test
    void createDraftOrder_createsHotelSnapshotWithNightAndRoomQuantity() {
        HotelEntity hotel = new HotelEntity();
        hotel.setId(15L);
        hotel.setNameEn("Da Nang Ocean Suites");

        RoomTypeEntity roomType = new RoomTypeEntity();
        roomType.setBasePrice(new BigDecimal("2400000"));
        hotel.setRoomTypes(new LinkedHashSet<>(Set.of(roomType)));

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(hotelRepository.findByIdAndDeletedFalseAndStatus(15L, "ACTIVE")).thenReturn(Optional.of(hotel));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> {
            OrderEntity order = invocation.getArgument(0);
            order.setId(1001L);
            return order;
        });

        CreateOrderRequest request = new CreateOrderRequest(
                "HOTEL",
                15L,
                "Family Suite",
                2,
                3,
                LocalDate.of(2026, 6, 10),
                LocalDate.of(2026, 6, 13),
                null,
                null,
                null,
                "Travel Customer",
                "0901234567",
                "customer@travelnest.test",
                "SUMMER2026",
                "VNPAY",
                "Late arrival"
        );

        OrderResponse response = orderService.createDraftOrder(authenticatedUser, request);

        assertThat(response.getId()).isEqualTo(1001L);
        assertThat(response.getServiceType()).isEqualTo("HOTEL");
        assertThat(response.getVariantName()).isEqualTo("Family Suite");
        assertThat(response.getQuantity()).isEqualTo(6);
        assertThat(response.getGuestCount()).isEqualTo(3);
        assertThat(response.getTotalAmount()).isEqualByComparingTo("14400000");
        assertThat(response.getPaymentMethod()).isEqualTo("VNPAY");
        assertThat(response.getVoucherCode()).isEqualTo("SUMMER2026");
    }

    @Test
    void createDraftOrder_usesMatchingTourSlotForDepartureDate() {
        TourEntity tour = new TourEntity();
        tour.setId(8L);
        tour.setNameEn("Ha Giang Loop Escape");

        TourSlotEntity slot = new TourSlotEntity();
        slot.setId(21L);
        slot.setStatus("OPEN");
        slot.setStartDate(LocalDate.of(2026, 6, 8));
        slot.setEndDate(LocalDate.of(2026, 6, 10));
        slot.setDepartureTime(LocalTime.of(6, 0));
        slot.setPricePerPerson(new BigDecimal("4290000"));
        tour.setSlots(new LinkedHashSet<>(Set.of(slot)));

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(tourRepository.findByIdAndDeletedFalseAndStatus(8L, "ACTIVE")).thenReturn(Optional.of(tour));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> {
            OrderEntity order = invocation.getArgument(0);
            order.setId(1002L);
            return order;
        });

        CreateOrderRequest request = new CreateOrderRequest(
                "TOUR",
                8L,
                null,
                null,
                2,
                null,
                null,
                LocalDate.of(2026, 6, 8),
                null,
                null,
                "Travel Customer",
                "0901234567",
                "customer@travelnest.test",
                null,
                "MOMO",
                null
        );

        OrderResponse response = orderService.createDraftOrder(authenticatedUser, request);

        assertThat(response.getVariantId()).isEqualTo(21L);
        assertThat(response.getStartDate()).isEqualTo(LocalDate.of(2026, 6, 8));
        assertThat(response.getEndDate()).isEqualTo(LocalDate.of(2026, 6, 10));
        assertThat(response.getServiceTime()).isEqualTo(LocalTime.of(6, 0));
        assertThat(response.getTotalAmount()).isEqualByComparingTo("8580000");
    }

    @Test
    void createDraftOrder_rejectsRestaurantReservationsOutsideOpeningHours() {
        RestaurantEntity restaurant = new RestaurantEntity();
        restaurant.setId(11L);
        restaurant.setNameEn("Ember Riverside Grill");
        restaurant.setOpenTime(LocalTime.of(11, 0));
        restaurant.setCloseTime(LocalTime.of(22, 30));

        MenuItemEntity item = new MenuItemEntity();
        item.setAvailable(true);
        item.setPrice(new BigDecimal("380000"));
        restaurant.setMenuItems(new LinkedHashSet<>(Set.of(item)));

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(restaurantRepository.findByIdAndDeletedFalseAndStatus(11L, "ACTIVE"))
                .thenReturn(Optional.of(restaurant));

        CreateOrderRequest request = new CreateOrderRequest(
                "RESTAURANT",
                11L,
                null,
                null,
                4,
                null,
                null,
                null,
                LocalDate.of(2026, 6, 15),
                LocalTime.of(23, 0),
                "Travel Customer",
                "0901234567",
                "customer@travelnest.test",
                null,
                "VNPAY",
                null
        );

        assertThatThrownBy(() -> orderService.createDraftOrder(authenticatedUser, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Reservation time must be within the restaurant operating hours");
    }
}
