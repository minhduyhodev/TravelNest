package com.travelnest.restaurant.repository;

import com.travelnest.restaurant.entity.RestaurantEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RestaurantRepository extends JpaRepository<RestaurantEntity, Long> {

    @Query("""
            select distinct r
            from RestaurantEntity r
            left join fetch r.menuItems mi
            where r.deleted = false
              and r.status = 'ACTIVE'
              and (
                  :keyword is null
                  or lower(r.nameVi) like lower(concat('%', :keyword, '%'))
                  or lower(r.nameEn) like lower(concat('%', :keyword, '%'))
                  or lower(r.cuisineTypeVi) like lower(concat('%', :keyword, '%'))
                  or lower(r.cuisineTypeEn) like lower(concat('%', :keyword, '%'))
                  or lower(r.district) like lower(concat('%', :keyword, '%'))
                  or lower(r.province) like lower(concat('%', :keyword, '%'))
                  or lower(r.descriptionVi) like lower(concat('%', :keyword, '%'))
                  or lower(r.descriptionEn) like lower(concat('%', :keyword, '%'))
              )
            order by r.createdAt desc
            """)
    List<RestaurantEntity> searchPublicRestaurants(@Param("keyword") String keyword);

    @EntityGraph(attributePaths = {"menuItems"})
    Optional<RestaurantEntity> findByIdAndDeletedFalseAndStatus(Long id, String status);

    @EntityGraph(attributePaths = {"menuCategories", "menuItems", "menuItems.category"})
    Optional<RestaurantEntity> findBySlugAndDeletedFalseAndStatus(String slug, String status);
}
