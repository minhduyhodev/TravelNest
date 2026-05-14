package com.travelnest.restaurant.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestaurantDetailResponse extends RestaurantSummaryResponse {

    private String address;
    private String phone;
    private String email;
    private Integer totalReviews;
    private List<String> menuCategories;
    private List<String> menuPreview;
    private List<String> policies;
}
