package com.travelnest.restaurant.controller;

import com.travelnest.common.api.ApiResponse;
import com.travelnest.restaurant.dto.RestaurantAvailabilityResponse;
import com.travelnest.restaurant.dto.RestaurantDetailResponse;
import com.travelnest.restaurant.dto.RestaurantSummaryResponse;
import com.travelnest.restaurant.service.RestaurantService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Restaurants")
@RestController
@RequestMapping("/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RestaurantSummaryResponse>>> getRestaurants(
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(ApiResponse.success(restaurantService.getPublicRestaurants(keyword)));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<RestaurantDetailResponse>> getRestaurantDetail(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(restaurantService.getPublicRestaurantDetail(slug)));
    }

    @GetMapping("/{slug}/availability")
    public ResponseEntity<ApiResponse<RestaurantAvailabilityResponse>> getRestaurantAvailability(
            @PathVariable String slug,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reservationDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime reservationTime,
            @RequestParam Integer guestCount
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                restaurantService.getAvailability(slug, reservationDate, reservationTime, guestCount)
        ));
    }
}
