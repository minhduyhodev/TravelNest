package com.travelnest.tour.service;

import com.travelnest.common.exception.ResourceNotFoundException;
import com.travelnest.tour.dto.TourDetailResponse;
import com.travelnest.tour.dto.TourSummaryResponse;
import com.travelnest.tour.entity.TourEntity;
import com.travelnest.tour.repository.TourRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TourService {

    private final TourRepository tourRepository;
    private final TourMapper tourMapper;

    public TourService(TourRepository tourRepository, TourMapper tourMapper) {
        this.tourRepository = tourRepository;
        this.tourMapper = tourMapper;
    }

    @Transactional(readOnly = true)
    public List<TourSummaryResponse> getPublicTours(String keyword) {
        return tourRepository.searchPublicTours(normalizeKeyword(keyword)).stream()
                .map(tourMapper::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public TourDetailResponse getPublicTourDetail(String slug) {
        TourEntity tour = tourRepository.findBySlugAndDeletedFalseAndStatus(slug, "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found"));
        return tourMapper.toDetail(tour);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
