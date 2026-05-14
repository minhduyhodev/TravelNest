package com.travelnest.hotel.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HotelDetailResponse extends HotelSummaryResponse {

    private String address;
    private String phone;
    private String email;
    private String checkInTime;
    private String checkOutTime;
    private Integer totalReviews;
    private List<String> amenities;
    private List<String> policies;
    private List<String> roomOptions;
}
