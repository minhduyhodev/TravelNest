package com.travelnest.hotel.service;

import com.travelnest.booking.repository.HotelBookingRepository;
import com.travelnest.common.exception.BadRequestException;
import com.travelnest.common.exception.ResourceNotFoundException;
import com.travelnest.hotel.dto.HotelAvailabilityResponse;
import com.travelnest.hotel.dto.HotelDetailResponse;
import com.travelnest.hotel.dto.HotelSummaryResponse;
import com.travelnest.hotel.entity.HotelEntity;
import com.travelnest.hotel.entity.RoomTypeEntity;
import com.travelnest.hotel.repository.HotelRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HotelService {

    private final HotelRepository hotelRepository;
    private final HotelBookingRepository hotelBookingRepository;
    private final HotelMapper hotelMapper;

    public HotelService(
            HotelRepository hotelRepository,
            HotelBookingRepository hotelBookingRepository,
            HotelMapper hotelMapper
    ) {
        this.hotelRepository = hotelRepository;
        this.hotelBookingRepository = hotelBookingRepository;
        this.hotelMapper = hotelMapper;
    }

    @Transactional(readOnly = true)
    public List<HotelSummaryResponse> getPublicHotels(String keyword) {
        return hotelRepository.searchPublicHotels(normalizeKeyword(keyword)).stream()
                .map(hotelMapper::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public HotelDetailResponse getPublicHotelDetail(String slug) {
        HotelEntity hotel = hotelRepository.findBySlugAndDeletedFalseAndStatus(slug, "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));
        return hotelMapper.toDetail(hotel);
    }

    @Transactional(readOnly = true)
    public HotelAvailabilityResponse getAvailability(
            String slug,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            Integer guestCount,
            Integer roomCount,
            String roomLabel
    ) {
        HotelEntity hotel = hotelRepository.findBySlugAndDeletedFalseAndStatus(slug, "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        if (checkInDate == null || checkOutDate == null) {
            throw new BadRequestException("Check-in and check-out dates are required");
        }
        if (!checkOutDate.isAfter(checkInDate)) {
            throw new BadRequestException("Check-out date must be after check-in date");
        }

        int requestedGuests = requirePositive(guestCount, "Guest count must be at least 1");
        int requestedRooms = requirePositive(roomCount, "Room count must be at least 1");

        Optional<RoomTypeEntity> preferredRoomType = hotel.getRoomTypes().stream()
                .filter(roomType -> !roomType.isDeleted())
                .filter(roomType -> roomType.getMaxGuests() != null && roomType.getMaxGuests() >= requestedGuests)
                .filter(roomType -> roomLabel == null || roomLabel.isBlank()
                        || roomLabel.equalsIgnoreCase(firstNonBlank(roomType.getNameEn(), roomType.getNameVi())))
                .findFirst();

        if (preferredRoomType.isEmpty()) {
            return new HotelAvailabilityResponse(
                    false,
                    roomLabel,
                    requestedRooms,
                    0,
                    "No room option can host the selected guest count for this stay."
            );
        }

        RoomTypeEntity roomType = preferredRoomType.get();
        int totalRooms = roomType.getTotalRooms() == null ? 0 : roomType.getTotalRooms();
        int reservedRooms = safeNumber(hotelBookingRepository.sumReservedRooms(
                hotel.getId(),
                roomType.getId(),
                checkInDate,
                checkOutDate
        ));
        int availableRooms = Math.max(0, totalRooms - reservedRooms);
        boolean available = availableRooms >= requestedRooms;

        return new HotelAvailabilityResponse(
                available,
                firstNonBlank(roomType.getNameEn(), roomType.getNameVi()),
                requestedRooms,
                availableRooms,
                available
                        ? "Rooms are available for the selected stay window."
                        : "Not enough rooms remain for the selected stay window."
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

    private String firstNonBlank(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred.trim();
        }
        return fallback == null ? "" : fallback.trim();
    }
}
