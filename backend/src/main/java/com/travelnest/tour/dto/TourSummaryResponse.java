package com.travelnest.tour.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TourSummaryResponse {

    private Long id;
    private String slug;
    private String name;
    private String location;
    private String description;
    private BigDecimal priceFrom;
    private BigDecimal rating;
    private String duration;
    private String departure;
}
