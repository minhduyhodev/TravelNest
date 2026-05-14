package com.travelnest.tour.service;

import com.travelnest.tour.dto.TourDetailResponse;
import com.travelnest.tour.dto.TourSummaryResponse;
import com.travelnest.tour.entity.TourEntity;
import com.travelnest.tour.entity.TourItineraryEntity;
import com.travelnest.tour.entity.TourSlotEntity;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class TourMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public TourSummaryResponse toSummary(TourEntity tour) {
        TourSummaryResponse response = new TourSummaryResponse();
        response.setId(tour.getId());
        response.setSlug(tour.getSlug());
        response.setName(firstNonBlank(tour.getNameEn(), tour.getNameVi()));
        response.setLocation(firstNonBlank(tour.getDestinationEn(), tour.getDestinationVi()));
        response.setDescription(firstNonBlank(tour.getDescriptionEn(), tour.getDescriptionVi()));
        response.setPriceFrom(tour.getSlots().stream()
                .filter(slot -> "OPEN".equalsIgnoreCase(slot.getStatus()))
                .map(TourSlotEntity::getPricePerPerson)
                .filter(price -> price != null)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO));
        response.setRating(tour.getAvgRating());
        response.setDuration(formatDuration(tour));
        response.setDeparture(formatDeparture(tour));
        return response;
    }

    public TourDetailResponse toDetail(TourEntity tour) {
        TourDetailResponse response = new TourDetailResponse();
        TourSummaryResponse summary = toSummary(tour);
        response.setId(summary.getId());
        response.setSlug(summary.getSlug());
        response.setName(summary.getName());
        response.setLocation(summary.getLocation());
        response.setDescription(summary.getDescription());
        response.setPriceFrom(summary.getPriceFrom());
        response.setRating(summary.getRating());
        response.setDuration(summary.getDuration());
        response.setDeparture(summary.getDeparture());
        response.setDeparturePoint(firstNonBlank(tour.getDeparturePointEn(), tour.getDeparturePointVi()));
        response.setTotalReviews(tour.getTotalReviews());
        response.setHighlights(splitCommaSeparated(firstNonBlank(tour.getIncludesEn(), tour.getIncludesVi())));
        response.setRequirements(splitCommaSeparated(firstNonBlank(tour.getRequirementsEn(), tour.getRequirementsVi())));
        response.setItinerary(tour.getItineraries().stream()
                .sorted(Comparator.comparing(TourItineraryEntity::getDayNumber))
                .map(this::formatItineraryItem)
                .toList());
        return response;
    }

    private String formatDuration(TourEntity tour) {
        int days = tour.getDurationDays() == null ? 0 : tour.getDurationDays();
        int nights = tour.getDurationNights() == null ? 0 : tour.getDurationNights();
        if (days <= 0 && nights <= 0) {
            return "";
        }
        if (nights <= 0) {
            return days + "D";
        }
        return days + "D" + nights + "N";
    }

    private String formatDeparture(TourEntity tour) {
        return tour.getSlots().stream()
                .filter(slot -> "OPEN".equalsIgnoreCase(slot.getStatus()))
                .min(Comparator.comparing(TourSlotEntity::getStartDate))
                .map(slot -> {
                    String date = slot.getStartDate() == null ? null : slot.getStartDate().format(DATE_FORMATTER);
                    String time = slot.getDepartureTime() == null ? null : slot.getDepartureTime().format(TIME_FORMATTER);
                    return Stream.of(date, time)
                            .filter(value -> value != null && !value.isBlank())
                            .reduce((left, right) -> left + " • " + right)
                            .orElse(firstNonBlank(slot.getNoteEn(), slot.getNoteVi()));
                })
                .orElse("");
    }

    private String formatItineraryItem(TourItineraryEntity itinerary) {
        String title = firstNonBlank(itinerary.getTitleEn(), itinerary.getTitleVi());
        String description = firstNonBlank(itinerary.getDescriptionEn(), itinerary.getDescriptionVi());
        if (description.isBlank()) {
            return "Day " + itinerary.getDayNumber() + ": " + title;
        }
        return "Day " + itinerary.getDayNumber() + ": " + title + " - " + description;
    }

    private List<String> splitCommaSeparated(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Stream.of(value.split(","))
                .map(String::trim)
                .filter(part -> !part.isBlank())
                .toList();
    }

    private String firstNonBlank(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred.trim();
        }
        return fallback == null ? "" : fallback.trim();
    }
}
