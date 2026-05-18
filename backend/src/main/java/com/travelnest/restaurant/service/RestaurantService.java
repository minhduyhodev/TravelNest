package com.travelnest.restaurant.service;

import com.travelnest.booking.repository.RestaurantBookingRepository;
import com.travelnest.common.exception.BadRequestException;
import com.travelnest.common.exception.ResourceNotFoundException;
import com.travelnest.restaurant.dto.RestaurantAvailabilityResponse;
import com.travelnest.restaurant.dto.RestaurantDetailResponse;
import com.travelnest.restaurant.dto.RestaurantSummaryResponse;
import com.travelnest.restaurant.entity.RestaurantEntity;
import com.travelnest.restaurant.repository.RestaurantRepository;
import com.travelnest.restaurant.repository.RestaurantTableRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final RestaurantBookingRepository restaurantBookingRepository;
    private final RestaurantMapper restaurantMapper;

    public RestaurantService(
            RestaurantRepository restaurantRepository,
            RestaurantTableRepository restaurantTableRepository,
            RestaurantBookingRepository restaurantBookingRepository,
            RestaurantMapper restaurantMapper
    ) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantTableRepository = restaurantTableRepository;
        this.restaurantBookingRepository = restaurantBookingRepository;
        this.restaurantMapper = restaurantMapper;
    }

    @Transactional(readOnly = true)
    public List<RestaurantSummaryResponse> getPublicRestaurants(String keyword) {
        return restaurantRepository.searchPublicRestaurants(normalizeKeyword(keyword)).stream()
                .map(restaurantMapper::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public RestaurantDetailResponse getPublicRestaurantDetail(String slug) {
        RestaurantEntity restaurant = restaurantRepository.findBySlugAndDeletedFalseAndStatus(slug, "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        return restaurantMapper.toDetail(restaurant);
    }

    @Transactional(readOnly = true)
    public RestaurantAvailabilityResponse getAvailability(
            String slug,
            LocalDate reservationDate,
            LocalTime reservationTime,
            Integer guestCount
    ) {
        RestaurantEntity restaurant = restaurantRepository.findBySlugAndDeletedFalseAndStatus(slug, "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        if (reservationDate == null || reservationTime == null) {
            throw new BadRequestException("Reservation date and time are required");
        }

        int requestedGuests = requirePositive(guestCount, "Party size must be at least 1");

        if (reservationTime.isBefore(restaurant.getOpenTime()) || reservationTime.isAfter(restaurant.getCloseTime())) {
            return new RestaurantAvailabilityResponse(
                    false,
                    requestedGuests,
                    0,
                    "The selected time is outside the restaurant opening hours."
            );
        }

        long activeTables = restaurantTableRepository.countActiveTablesByCapacity(restaurant.getId(), requestedGuests);
        if (activeTables < 1) {
            return new RestaurantAvailabilityResponse(
                    false,
                    requestedGuests,
                    0,
                    "No active table can seat the selected party size."
            );
        }

        LocalTime windowStart = reservationTime.minusHours(2);
        LocalTime windowEnd = reservationTime.plusHours(2);
        long conflictingReservations = restaurantBookingRepository.countConflictingReservations(
                restaurant.getId(),
                reservationDate,
                windowStart.isAfter(LocalTime.MIN) ? windowStart : LocalTime.MIN,
                windowEnd.isBefore(LocalTime.MAX) ? windowEnd : LocalTime.MAX
        );
        int availableTables = (int) Math.max(0, activeTables - conflictingReservations);
        boolean available = availableTables > 0;

        return new RestaurantAvailabilityResponse(
                available,
                requestedGuests,
                availableTables,
                available
                        ? "Tables are available around the selected reservation time."
                        : "No compatible table is available around the selected reservation time."
        );
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    private int requirePositive(Integer value, String message) {
        if (value == null || value < 1) {
            throw new BadRequestException(message);
        }
        return value;
    }
}
