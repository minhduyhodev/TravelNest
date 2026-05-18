package com.travelnest.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HotelAvailabilityResponse {

    private boolean available;
    private String roomLabel;
    private Integer requestedRooms;
    private Integer availableRooms;
    private String message;
}
