package com.travelnest.restaurant.repository;

import com.travelnest.restaurant.entity.RestaurantTableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTableEntity, Long> {

    @Query("""
            select count(rt)
            from RestaurantTableEntity rt
            where rt.restaurant.id = :restaurantId
              and rt.active = true
              and rt.capacity >= :guestCount
            """)
    long countActiveTablesByCapacity(
            @Param("restaurantId") Long restaurantId,
            @Param("guestCount") int guestCount
    );
}
