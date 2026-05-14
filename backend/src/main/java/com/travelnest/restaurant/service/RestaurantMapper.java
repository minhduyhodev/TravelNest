package com.travelnest.restaurant.service;

import com.travelnest.restaurant.dto.RestaurantDetailResponse;
import com.travelnest.restaurant.dto.RestaurantSummaryResponse;
import com.travelnest.restaurant.entity.MenuItemEntity;
import com.travelnest.restaurant.entity.RestaurantEntity;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class RestaurantMapper {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public RestaurantSummaryResponse toSummary(RestaurantEntity restaurant) {
        RestaurantSummaryResponse response = new RestaurantSummaryResponse();
        response.setId(restaurant.getId());
        response.setSlug(restaurant.getSlug());
        response.setName(firstNonBlank(restaurant.getNameEn(), restaurant.getNameVi()));
        response.setLocation(buildLocation(restaurant));
        response.setDescription(firstNonBlank(restaurant.getDescriptionEn(), restaurant.getDescriptionVi()));
        response.setPriceFrom(restaurant.getMenuItems().stream()
                .filter(item -> item.isAvailable() && !item.isDeleted())
                .map(MenuItemEntity::getPrice)
                .filter(price -> price != null)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO));
        response.setRating(restaurant.getAvgRating());
        response.setCuisine(firstNonBlank(restaurant.getCuisineTypeEn(), restaurant.getCuisineTypeVi()));
        response.setSchedule(formatSchedule(restaurant));
        return response;
    }

    public RestaurantDetailResponse toDetail(RestaurantEntity restaurant) {
        RestaurantDetailResponse response = new RestaurantDetailResponse();
        RestaurantSummaryResponse summary = toSummary(restaurant);
        response.setId(summary.getId());
        response.setSlug(summary.getSlug());
        response.setName(summary.getName());
        response.setLocation(summary.getLocation());
        response.setDescription(summary.getDescription());
        response.setPriceFrom(summary.getPriceFrom());
        response.setRating(summary.getRating());
        response.setCuisine(summary.getCuisine());
        response.setSchedule(summary.getSchedule());
        response.setAddress(restaurant.getAddress());
        response.setPhone(restaurant.getPhone());
        response.setEmail(restaurant.getEmail());
        response.setTotalReviews(restaurant.getTotalReviews());
        response.setMenuCategories(restaurant.getMenuCategories().stream()
                .map(category -> firstNonBlank(category.getNameEn(), category.getNameVi()))
                .toList());
        response.setMenuPreview(restaurant.getMenuItems().stream()
                .filter(item -> item.isAvailable() && !item.isDeleted())
                .sorted(Comparator.comparing(MenuItemEntity::getSortOrder))
                .map(item -> firstNonBlank(item.getNameEn(), item.getNameVi()))
                .limit(5)
                .toList());
        response.setPolicies(buildPolicies(restaurant));
        return response;
    }

    private List<String> buildPolicies(RestaurantEntity restaurant) {
        String schedule = "Open daily: " + formatSchedule(restaurant);
        String priceRange = restaurant.getPriceRange() == null || restaurant.getPriceRange().isBlank()
                ? null
                : "Price range: " + restaurant.getPriceRange();
        String cancelPolicy = firstNonBlank(restaurant.getCancelPolicyEn(), restaurant.getCancelPolicyVi());

        return Stream.of(schedule, priceRange, cancelPolicy)
                .filter(value -> value != null && !value.isBlank())
                .toList();
    }

    private String formatSchedule(RestaurantEntity restaurant) {
        String open = restaurant.getOpenTime() == null ? null : restaurant.getOpenTime().format(TIME_FORMATTER);
        String close = restaurant.getCloseTime() == null ? null : restaurant.getCloseTime().format(TIME_FORMATTER);
        return Stream.of(open, close)
                .filter(value -> value != null && !value.isBlank())
                .reduce((left, right) -> left + " - " + right)
                .orElse("");
    }

    private String buildLocation(RestaurantEntity restaurant) {
        return List.of(restaurant.getDistrict(), restaurant.getProvince()).stream()
                .filter(value -> value != null && !value.isBlank())
                .map(this::capitalizeWords)
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
