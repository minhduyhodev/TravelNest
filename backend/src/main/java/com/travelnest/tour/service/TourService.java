package com.travelnest.tour.service;

import com.travelnest.booking.repository.TourBookingRepository;
import com.travelnest.common.exception.BadRequestException;
import com.travelnest.common.exception.ResourceNotFoundException;
import com.travelnest.tour.dto.TourAvailabilityResponse;
import com.travelnest.tour.dto.TourDetailResponse;
import com.travelnest.tour.dto.TourSummaryResponse;
import com.travelnest.tour.entity.TourEntity;
import com.travelnest.tour.entity.TourSlotEntity;
import com.travelnest.tour.repository.TourRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TourService {

    private final TourRepository tourRepository;
    private final TourBookingRepository tourBookingRepository;
    private final TourMapper tourMapper;

    public TourService(
            TourRepository tourRepository,
            TourBookingRepository tourBookingRepository,
            TourMapper tourMapper
    ) {
        this.tourRepository = tourRepository;
        this.tourBookingRepository = tourBookingRepository;
        this.tourMapper = tourMapper;
    }

    @Transactional(readOnly = true)
    public List<TourSummaryResponse> getPublicTours(String keyword) {
        return tourRepository.searchPublicTours(normalizeKeyword(keyword)).stream()
                .map(tourMapper::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public TourDetailResponse getPublicTourDetail(String slug) {
        TourEntity tour = tourRepository.findBySlugAndDeletedFalseAndStatus(slug, "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found"));
        return tourMapper.toDetail(tour);
    }

    @Transactional(readOnly = true)
    public TourAvailabilityResponse getAvailability(String slug, LocalDate departureDate, Integer guestCount) {
        TourEntity tour = tourRepository.findBySlugAndDeletedFalseAndStatus(slug, "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found"));

        if (departureDate == null) {
            throw new BadRequestException("Departure date is required");
        }

        int requestedGuests = requirePositive(guestCount, "Traveler count must be at least 1");
        TourSlotEntity slot = tour.getSlots().stream()
                .filter(candidate -> "OPEN".equalsIgnoreCase(candidate.getStatus()))
                .filter(candidate -> departureDate.equals(candidate.getStartDate()))
                .findFirst()
                .orElse(null);

        if (slot == null) {
            return new TourAvailabilityResponse(
                    false,
                    null,
                    requestedGuests,
                    0,
                    "No open departure is available on the selected date."
            );
        }

        int bookedFromSeed = slot.getBookedSlots() == null ? 0 : slot.getBookedSlots();
        int bookedFromCurrentOrders = safeNumber(tourBookingRepository.sumReservedGuests(slot.getId()));
        int availableSeats = Math.max(0, safeNumber(slot.getTotalSlots()) - bookedFromSeed - bookedFromCurrentOrders);
        boolean available = availableSeats >= requestedGuests;

        return new TourAvailabilityResponse(
                available,
                slot.getId(),
                requestedGuests,
                availableSeats,
                available
                        ? "Seats are available for the selected departure."
                        : "Not enough seats remain for the selected departure."
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

    private int safeNumber(Integer value) {
        return value == null ? 0 : value;
    }
}
