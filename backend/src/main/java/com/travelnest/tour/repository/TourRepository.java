package com.travelnest.tour.repository;

import com.travelnest.tour.entity.TourEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TourRepository extends JpaRepository<TourEntity, Long> {

    @Query("""
            select distinct t
            from TourEntity t
            left join fetch t.slots s
            where t.deleted = false
              and t.status = 'ACTIVE'
              and (
                  :keyword is null
                  or lower(t.nameVi) like lower(concat('%', :keyword, '%'))
                  or lower(t.nameEn) like lower(concat('%', :keyword, '%'))
                  or lower(t.destinationVi) like lower(concat('%', :keyword, '%'))
                  or lower(t.destinationEn) like lower(concat('%', :keyword, '%'))
                  or lower(t.departurePointVi) like lower(concat('%', :keyword, '%'))
                  or lower(t.departurePointEn) like lower(concat('%', :keyword, '%'))
                  or lower(t.descriptionVi) like lower(concat('%', :keyword, '%'))
                  or lower(t.descriptionEn) like lower(concat('%', :keyword, '%'))
              )
            order by t.createdAt desc
            """)
    List<TourEntity> searchPublicTours(@Param("keyword") String keyword);

    @EntityGraph(attributePaths = {"slots"})
    Optional<TourEntity> findByIdAndDeletedFalseAndStatus(Long id, String status);

    @EntityGraph(attributePaths = {"itineraries", "slots"})
    Optional<TourEntity> findBySlugAndDeletedFalseAndStatus(String slug, String status);
}
