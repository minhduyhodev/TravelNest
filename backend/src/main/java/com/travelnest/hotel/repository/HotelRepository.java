package com.travelnest.hotel.repository;

import com.travelnest.hotel.entity.HotelEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HotelRepository extends JpaRepository<HotelEntity, Long> {

    @Query("""
            select distinct h
            from HotelEntity h
            left join fetch h.roomTypes rt
            where h.deleted = false
              and h.status = 'ACTIVE'
              and (
                  :keyword is null
                  or lower(h.nameVi) like lower(concat('%', :keyword, '%'))
                  or lower(h.nameEn) like lower(concat('%', :keyword, '%'))
                  or lower(h.district) like lower(concat('%', :keyword, '%'))
                  or lower(h.province) like lower(concat('%', :keyword, '%'))
                  or lower(h.descriptionVi) like lower(concat('%', :keyword, '%'))
                  or lower(h.descriptionEn) like lower(concat('%', :keyword, '%'))
              )
            order by h.createdAt desc
            """)
    List<HotelEntity> searchPublicHotels(@Param("keyword") String keyword);

    @EntityGraph(attributePaths = {"roomTypes"})
    Optional<HotelEntity> findByIdAndDeletedFalseAndStatus(Long id, String status);

    @EntityGraph(attributePaths = {"roomTypes", "amenities"})
    Optional<HotelEntity> findBySlugAndDeletedFalseAndStatus(String slug, String status);
}
