package com.travelnest.hotel.controller;

import com.travelnest.common.api.ApiResponse;
import com.travelnest.hotel.dto.HotelDetailResponse;
import com.travelnest.hotel.dto.HotelSummaryResponse;
import com.travelnest.hotel.service.HotelService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Hotels")
@RestController
@RequestMapping("/hotels")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<HotelSummaryResponse>>> getHotels(
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(ApiResponse.success(hotelService.getPublicHotels(keyword)));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<HotelDetailResponse>> getHotelDetail(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(hotelService.getPublicHotelDetail(slug)));
    }
}
