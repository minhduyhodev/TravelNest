package com.travelnest.tour.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TourAvailabilityResponse {

    private boolean available;
    private Long slotId;
    private Integer requestedGuests;
    private Integer availableSeats;
    private String message;
}
