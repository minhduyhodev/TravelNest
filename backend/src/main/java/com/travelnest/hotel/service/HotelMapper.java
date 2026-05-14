package com.travelnest.hotel.service;

import com.travelnest.hotel.dto.HotelDetailResponse;
import com.travelnest.hotel.dto.HotelSummaryResponse;
import com.travelnest.hotel.entity.HotelEntity;
import com.travelnest.hotel.entity.RoomTypeEntity;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class HotelMapper {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public HotelSummaryResponse toSummary(HotelEntity hotel) {
        HotelSummaryResponse response = new HotelSummaryResponse();
        response.setId(hotel.getId());
        response.setSlug(hotel.getSlug());
        response.setName(firstNonBlank(hotel.getNameEn(), hotel.getNameVi()));
        response.setLocation(buildLocation(hotel));
        response.setDescription(firstNonBlank(hotel.getDescriptionEn(), hotel.getDescriptionVi()));
        response.setPriceFrom(hotel.getRoomTypes().stream()
                .filter(roomType -> !roomType.isDeleted())
                .map(RoomTypeEntity::getBasePrice)
                .filter(price -> price != null)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO));
        response.setRating(hotel.getAvgRating());
        response.setStarRating(hotel.getStarRating() == null ? null : (int) hotel.getStarRating());
        return response;
    }

    public HotelDetailResponse toDetail(HotelEntity hotel) {
        HotelDetailResponse response = new HotelDetailResponse();
        HotelSummaryResponse summary = toSummary(hotel);
        response.setId(summary.getId());
        response.setSlug(summary.getSlug());
        response.setName(summary.getName());
        response.setLocation(summary.getLocation());
        response.setDescription(summary.getDescription());
        response.setPriceFrom(summary.getPriceFrom());
        response.setRating(summary.getRating());
        response.setStarRating(summary.getStarRating());
        response.setAddress(hotel.getAddress());
        response.setPhone(hotel.getPhone());
        response.setEmail(hotel.getEmail());
        response.setCheckInTime(hotel.getCheckInTime() == null ? null : hotel.getCheckInTime().format(TIME_FORMATTER));
        response.setCheckOutTime(hotel.getCheckOutTime() == null ? null : hotel.getCheckOutTime().format(TIME_FORMATTER));
        response.setTotalReviews(hotel.getTotalReviews());
        response.setAmenities(hotel.getAmenities().stream()
                .map(amenity -> firstNonBlank(amenity.getNameEn(), amenity.getNameVi()))
                .toList());
        response.setPolicies(buildPolicies(hotel));
        response.setRoomOptions(hotel.getRoomTypes().stream()
                .filter(roomType -> !roomType.isDeleted())
                .map(roomType -> firstNonBlank(roomType.getNameEn(), roomType.getNameVi()))
                .toList());
        return response;
    }

    private List<String> buildPolicies(HotelEntity hotel) {
        String checkIn = hotel.getCheckInTime() == null ? null : "Check-in from " + hotel.getCheckInTime().format(TIME_FORMATTER);
        String checkOut = hotel.getCheckOutTime() == null ? null : "Check-out before " + hotel.getCheckOutTime().format(TIME_FORMATTER);
        String cancelPolicy = firstNonBlank(hotel.getCancelPolicyEn(), hotel.getCancelPolicyVi());

        return Stream.of(checkIn, checkOut, cancelPolicy)
                .filter(value -> value != null && !value.isBlank())
                .toList();
    }

    private String buildLocation(HotelEntity hotel) {
        return List.of(hotel.getDistrict(), hotel.getProvince()).stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> capitalizeWords(value.trim()))
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private String capitalizeWords(String value) {
        String[] parts = value.toLowerCase(Locale.ROOT).split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.toString();
    }

    private String firstNonBlank(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred.trim();
        }
        return fallback == null ? "" : fallback.trim();
    }
}
