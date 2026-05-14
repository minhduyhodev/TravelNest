package com.travelnest.tour.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TourDetailResponse extends TourSummaryResponse {

    private String departurePoint;
    private Integer totalReviews;
    private List<String> highlights;
    private List<String> itinerary;
    private List<String> requirements;
}
