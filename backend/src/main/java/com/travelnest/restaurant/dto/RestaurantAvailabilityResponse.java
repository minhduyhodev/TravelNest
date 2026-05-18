package com.travelnest.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantAvailabilityResponse {

    private boolean available;
    private Integer requestedGuests;
    private Integer availableTables;
    private String message;
}
