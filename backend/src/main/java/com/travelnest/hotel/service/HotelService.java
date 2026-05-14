package com.travelnest.hotel.service;

import com.travelnest.common.exception.ResourceNotFoundException;
import com.travelnest.hotel.dto.HotelDetailResponse;
import com.travelnest.hotel.dto.HotelSummaryResponse;
import com.travelnest.hotel.entity.HotelEntity;
import com.travelnest.hotel.repository.HotelRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HotelService {

    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;

    public HotelService(HotelRepository hotelRepository, HotelMapper hotelMapper) {
        this.hotelRepository = hotelRepository;
        this.hotelMapper = hotelMapper;
    }

    @Transactional(readOnly = true)
    public List<HotelSummaryResponse> getPublicHotels(String keyword) {
        return hotelRepository.searchPublicHotels(normalizeKeyword(keyword)).stream()
                .map(hotelMapper::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public HotelDetailResponse getPublicHotelDetail(String slug) {
        HotelEntity hotel = hotelRepository.findBySlugAndDeletedFalseAndStatus(slug, "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));
        return hotelMapper.toDetail(hotel);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
