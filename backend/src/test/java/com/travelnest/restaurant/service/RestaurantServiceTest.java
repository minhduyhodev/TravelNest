package com.travelnest.restaurant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.travelnest.booking.repository.RestaurantBookingRepository;
import com.travelnest.restaurant.dto.RestaurantAvailabilityResponse;
import com.travelnest.restaurant.dto.RestaurantDetailResponse;
import com.travelnest.restaurant.dto.RestaurantSummaryResponse;
import com.travelnest.restaurant.entity.MenuCategoryEntity;
import com.travelnest.restaurant.entity.MenuItemEntity;
import com.travelnest.restaurant.entity.RestaurantEntity;
import com.travelnest.restaurant.repository.RestaurantRepository;
import com.travelnest.restaurant.repository.RestaurantTableRepository;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RestaurantTableRepository restaurantTableRepository;

    @Mock
    private RestaurantBookingRepository restaurantBookingRepository;

    private RestaurantService restaurantService;

    @BeforeEach
    void setUp() {
        restaurantService = new RestaurantService(
                restaurantRepository,
                restaurantTableRepository,
                restaurantBookingRepository,
                new RestaurantMapper()
        );
    }

    @Test
    void getPublicRestaurants_mapsScheduleCuisineAndPrice() {
        RestaurantEntity restaurant = buildRestaurant();

        when(restaurantRepository.searchPublicRestaurants("grill")).thenReturn(List.of(restaurant));

        List<RestaurantSummaryResponse> response = restaurantService.getPublicRestaurants("grill");

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().getSlug()).isEqualTo("ember-riverside-grill");
        assertThat(response.getFirst().getSchedule()).isEqualTo("11:00 - 22:30");
        assertThat(response.getFirst().getCuisine()).isEqualTo("Seafood and grill");
        assertThat(response.getFirst().getPriceFrom()).isEqualByComparingTo("380000");
    }

    @Test
    void getPublicRestaurantDetail_returnsMenuAndPolicies() {
        RestaurantEntity restaurant = buildRestaurant();

        when(restaurantRepository.findBySlugAndDeletedFalseAndStatus("ember-riverside-grill", "ACTIVE"))
                .thenReturn(Optional.of(restaurant));

        RestaurantDetailResponse response = restaurantService.getPublicRestaurantDetail("ember-riverside-grill");

        assertThat(response.getMenuCategories()).containsExactly("Signature grill", "Seafood sharing");
        assertThat(response.getMenuPreview()).containsExactly("Charred squid skewers", "Signature river prawns");
        assertThat(response.getPolicies()).anyMatch(policy -> policy.contains("Open daily"));
    }

    @Test
    void getAvailability_returnsTableCountForReservationWindow() {
        RestaurantEntity restaurant = buildRestaurant();

        when(restaurantRepository.findBySlugAndDeletedFalseAndStatus("ember-riverside-grill", "ACTIVE"))
                .thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.countActiveTablesByCapacity(11L, 4)).thenReturn(2L);
        when(restaurantBookingRepository.countConflictingReservations(
                eq(11L),
                eq(java.time.LocalDate.of(2026, 6, 10)),
                eq(java.time.LocalTime.of(17, 0)),
                eq(java.time.LocalTime.of(21, 0))
        )).thenReturn(1L);

        RestaurantAvailabilityResponse response = restaurantService.getAvailability(
                "ember-riverside-grill",
                java.time.LocalDate.of(2026, 6, 10),
                java.time.LocalTime.of(19, 0),
                4
        );

        assertThat(response.isAvailable()).isTrue();
        assertThat(response.getAvailableTables()).isEqualTo(1);
    }

    private RestaurantEntity buildRestaurant() {
        RestaurantEntity restaurant = new RestaurantEntity();
        restaurant.setId(11L);
        restaurant.setSlug("ember-riverside-grill");
        restaurant.setNameEn("Ember Riverside Grill");
        restaurant.setDescriptionEn("Fire-grilled seafood by the river.");
        restaurant.setCuisineTypeEn("Seafood and grill");
        restaurant.setDistrict("Hai Chau");
        restaurant.setProvince("Da Nang");
        restaurant.setAddress("18 Bach Dang");
        restaurant.setOpenTime(LocalTime.of(11, 0));
        restaurant.setCloseTime(LocalTime.of(22, 30));
        restaurant.setPriceRange("PREMIUM");
        restaurant.setCancelPolicyEn("Tables are held for 15 minutes.");
        restaurant.setAvgRating(new BigDecimal("9.00"));
        restaurant.setTotalReviews(142);

        MenuCategoryEntity signature = new MenuCategoryEntity();
        signature.setNameEn("Signature grill");
        signature.setSortOrder(1);

        MenuCategoryEntity seafood = new MenuCategoryEntity();
        seafood.setNameEn("Seafood sharing");
        seafood.setSortOrder(2);

        MenuItemEntity squid = new MenuItemEntity();
        squid.setNameEn("Charred squid skewers");
        squid.setPrice(new BigDecimal("380000"));
        squid.setSortOrder(1);
        squid.setAvailable(true);

        MenuItemEntity prawns = new MenuItemEntity();
        prawns.setNameEn("Signature river prawns");
        prawns.setPrice(new BigDecimal("520000"));
        prawns.setSortOrder(2);
        prawns.setAvailable(true);

        restaurant.setMenuCategories(new LinkedHashSet<>(List.of(signature, seafood)));
        restaurant.setMenuItems(new LinkedHashSet<>(List.of(squid, prawns)));
        return restaurant;
    }
}
