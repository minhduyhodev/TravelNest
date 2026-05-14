package com.travelnest.restaurant.service;

import com.travelnest.common.exception.ResourceNotFoundException;
import com.travelnest.restaurant.dto.RestaurantDetailResponse;
import com.travelnest.restaurant.dto.RestaurantSummaryResponse;
import com.travelnest.restaurant.entity.RestaurantEntity;
import com.travelnest.restaurant.repository.RestaurantRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantMapper restaurantMapper;

    public RestaurantService(RestaurantRepository restaurantRepository, RestaurantMapper restaurantMapper) {
        this.restaurantRepository = restaurantRepository;
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

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
