package com.travelnest.tour.controller;

import com.travelnest.common.api.ApiResponse;
import com.travelnest.tour.dto.TourAvailabilityResponse;
import com.travelnest.tour.dto.TourDetailResponse;
import com.travelnest.tour.dto.TourSummaryResponse;
import com.travelnest.tour.service.TourService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Tours")
@RestController
@RequestMapping("/tours")
public class TourController {

    private final TourService tourService;

    public TourController(TourService tourService) {
        this.tourService = tourService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TourSummaryResponse>>> getTours(
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(ApiResponse.success(tourService.getPublicTours(keyword)));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<TourDetailResponse>> getTourDetail(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(tourService.getPublicTourDetail(slug)));
    }

    @GetMapping("/{slug}/availability")
    public ResponseEntity<ApiResponse<TourAvailabilityResponse>> getTourAvailability(
            @PathVariable String slug,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate,
            @RequestParam Integer guestCount
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                tourService.getAvailability(slug, departureDate, guestCount)
        ));
    }
}
