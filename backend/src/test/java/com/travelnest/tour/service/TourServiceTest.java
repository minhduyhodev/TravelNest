package com.travelnest.tour.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.travelnest.tour.dto.TourDetailResponse;
import com.travelnest.tour.dto.TourSummaryResponse;
import com.travelnest.tour.entity.TourEntity;
import com.travelnest.tour.entity.TourItineraryEntity;
import com.travelnest.tour.entity.TourSlotEntity;
import com.travelnest.tour.repository.TourRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
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
class TourServiceTest {

    @Mock
    private TourRepository tourRepository;

    private TourService tourService;

    @BeforeEach
    void setUp() {
        tourService = new TourService(tourRepository, new TourMapper());
    }

    @Test
    void getPublicTours_mapsDurationDepartureAndPrice() {
        TourEntity tour = buildTour();

        when(tourRepository.searchPublicTours("ha giang")).thenReturn(List.of(tour));

        List<TourSummaryResponse> response = tourService.getPublicTours("ha giang");

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().getSlug()).isEqualTo("ha-giang-loop-escape");
        assertThat(response.getFirst().getDuration()).isEqualTo("3D2N");
        assertThat(response.getFirst().getPriceFrom()).isEqualByComparingTo("4290000");
        assertThat(response.getFirst().getDeparture()).contains("01 Jun 2026");
    }

    @Test
    void getPublicTourDetail_returnsHighlightsAndItinerary() {
        TourEntity tour = buildTour();

        when(tourRepository.findBySlugAndDeletedFalseAndStatus("ha-giang-loop-escape", "ACTIVE"))
                .thenReturn(Optional.of(tour));

        TourDetailResponse response = tourService.getPublicTourDetail("ha-giang-loop-escape");

        assertThat(response.getHighlights()).containsExactly(
                "Ma Pi Leng Pass",
                "Ethnic village homestay",
                "Photo support",
                "Breakfast and dinner"
        );
        assertThat(response.getItinerary()).hasSize(2);
        assertThat(response.getRequirements()).containsExactly("Bring a light jacket");
    }

    private TourEntity buildTour() {
        TourEntity tour = new TourEntity();
        tour.setId(7L);
        tour.setSlug("ha-giang-loop-escape");
        tour.setNameEn("Ha Giang Loop Escape");
        tour.setDestinationEn("Ha Giang");
        tour.setDescriptionEn("Three-day highland circuit.");
        tour.setDurationDays((byte) 3);
        tour.setDurationNights((byte) 2);
        tour.setDeparturePointEn("Ha Giang City");
        tour.setIncludesEn("Ma Pi Leng Pass, Ethnic village homestay, Photo support, Breakfast and dinner");
        tour.setRequirementsEn("Bring a light jacket");
        tour.setAvgRating(new BigDecimal("9.50"));
        tour.setTotalReviews(121);

        TourSlotEntity slot = new TourSlotEntity();
        slot.setStatus("OPEN");
        slot.setStartDate(LocalDate.of(2026, 6, 1));
        slot.setDepartureTime(LocalTime.of(6, 0));
        slot.setPricePerPerson(new BigDecimal("4290000"));

        TourItineraryEntity firstDay = new TourItineraryEntity();
        firstDay.setDayNumber((byte) 1);
        firstDay.setTitleEn("City to Yen Minh");
        firstDay.setDescriptionEn("Depart from the city.");

        TourItineraryEntity secondDay = new TourItineraryEntity();
        secondDay.setDayNumber((byte) 2);
        secondDay.setTitleEn("Dong Van to Meo Vac");
        secondDay.setDescriptionEn("Cross Ma Pi Leng Pass.");

        tour.setSlots(new LinkedHashSet<>(List.of(slot)));
        tour.setItineraries(new LinkedHashSet<>(List.of(firstDay, secondDay)));
        return tour;
    }
}
