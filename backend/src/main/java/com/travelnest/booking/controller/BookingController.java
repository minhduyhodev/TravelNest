package com.travelnest.booking.controller;

import com.travelnest.booking.dto.BookingResponse;
import com.travelnest.booking.dto.BookingActionRequest;
import com.travelnest.booking.dto.CreateBookingRequest;
import com.travelnest.booking.service.BookingService;
import com.travelnest.common.api.ApiResponse;
import com.travelnest.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Bookings")
@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getCurrentUserBookings(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getCurrentUserBookings(authenticatedUser)));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingDetail(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long bookingId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                bookingService.getBookingDetail(authenticatedUser, bookingId)
        ));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getManagementBookings(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate serviceDate
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                bookingService.getManagementBookings(status, serviceType, serviceDate)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody CreateBookingRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Booking created successfully",
                bookingService.createBooking(authenticatedUser, request)
        ));
    }

    @PatchMapping("/{bookingId}/confirm")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmBooking(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long bookingId,
            @Valid @RequestBody(required = false) BookingActionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Booking confirmed successfully",
                bookingService.confirmBooking(authenticatedUser, bookingId, request)
        ));
    }

    @PatchMapping("/{bookingId}/cancel")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long bookingId,
            @Valid @RequestBody(required = false) BookingActionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Booking cancelled successfully",
                bookingService.cancelBooking(authenticatedUser, bookingId, request)
        ));
    }

    @PatchMapping("/{bookingId}/complete")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponse>> completeBooking(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long bookingId,
            @Valid @RequestBody(required = false) BookingActionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Booking completed successfully",
                bookingService.completeBooking(authenticatedUser, bookingId, request)
        ));
    }
}
