package com.travelnest.hotel.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.travelnest.hotel.dto.HotelDetailResponse;
import com.travelnest.hotel.dto.HotelSummaryResponse;
import com.travelnest.hotel.entity.HotelAmenityEntity;
import com.travelnest.hotel.entity.HotelEntity;
import com.travelnest.hotel.entity.RoomTypeEntity;
import com.travelnest.hotel.repository.HotelRepository;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    private HotelService hotelService;

    @BeforeEach
    void setUp() {
        hotelService = new HotelService(hotelRepository, new HotelMapper());
    }

    @Test
    void getPublicHotels_mapsPriceFromAndLocation() {
        HotelEntity hotel = buildHotel();

        when(hotelRepository.searchPublicHotels("da nang")).thenReturn(List.of(hotel));

        List<HotelSummaryResponse> response = hotelService.getPublicHotels("da nang");

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().getSlug()).isEqualTo("da-nang-ocean-suites");
        assertThat(response.getFirst().getPriceFrom()).isEqualByComparingTo("2400000");
        assertThat(response.getFirst().getLocation()).isEqualTo("Son Tra, Da Nang");
    }

    @Test
    void getPublicHotelDetail_returnsAmenitiesPoliciesAndRooms() {
        HotelEntity hotel = buildHotel();

        when(hotelRepository.findBySlugAndDeletedFalseAndStatus("da-nang-ocean-suites", "ACTIVE"))
                .thenReturn(Optional.of(hotel));

        HotelDetailResponse response = hotelService.getPublicHotelDetail("da-nang-ocean-suites");

        assertThat(response.getAmenities()).containsExactly("Infinity pool", "Breakfast included");
        assertThat(response.getRoomOptions()).containsExactly("Deluxe Ocean", "Family Suite");
        assertThat(response.getPolicies()).anyMatch(policy -> policy.contains("Check-in from 14:00"));
        assertThat(response.getPolicies()).anyMatch(policy -> policy.contains("48 hours"));
    }

    private HotelEntity buildHotel() {
        HotelEntity hotel = new HotelEntity();
        hotel.setId(10L);
        hotel.setSlug("da-nang-ocean-suites");
        hotel.setNameVi("Da Nang Ocean Suites");
        hotel.setNameEn("Da Nang Ocean Suites");
        hotel.setDescriptionVi("Khach san bien.");
        hotel.setDescriptionEn("Modern beachfront stay.");
        hotel.setDistrict("Son Tra");
        hotel.setProvince("Da Nang");
        hotel.setAddress("27 Vo Nguyen Giap");
        hotel.setCheckInTime(LocalTime.of(14, 0));
        hotel.setCheckOutTime(LocalTime.of(12, 0));
        hotel.setCancelPolicyEn("Free cancellation up to 48 hours before arrival.");
        hotel.setAvgRating(new BigDecimal("9.20"));
        hotel.setTotalReviews(184);
        hotel.setStarRating((byte) 5);

        RoomTypeEntity deluxe = new RoomTypeEntity();
        deluxe.setNameEn("Deluxe Ocean");
        deluxe.setBasePrice(new BigDecimal("2400000"));

        RoomTypeEntity family = new RoomTypeEntity();
        family.setNameEn("Family Suite");
        family.setBasePrice(new BigDecimal("3100000"));

        hotel.setRoomTypes(new LinkedHashSet<>(List.of(deluxe, family)));

        HotelAmenityEntity pool = new HotelAmenityEntity();
        pool.setNameEn("Infinity pool");

        HotelAmenityEntity breakfast = new HotelAmenityEntity();
        breakfast.setNameEn("Breakfast included");

        hotel.setAmenities(new LinkedHashSet<>(List.of(pool, breakfast)));
        return hotel;
    }
}
