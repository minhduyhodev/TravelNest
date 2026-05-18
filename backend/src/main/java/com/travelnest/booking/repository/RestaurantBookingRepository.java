package com.travelnest.booking.repository;

import com.travelnest.booking.entity.RestaurantBookingEntity;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RestaurantBookingRepository extends JpaRepository<RestaurantBookingEntity, Long> {

    @Query("""
            select count(rb)
            from RestaurantBookingEntity rb
            where rb.restaurant.id = :restaurantId
              and rb.booking.status <> 'CANCELLED'
              and rb.reservationDate = :reservationDate
              and rb.reservationTime >= :windowStart
              and rb.reservationTime < :windowEnd
            """)
    long countConflictingReservations(
            @Param("restaurantId") Long restaurantId,
            @Param("reservationDate") LocalDate reservationDate,
            @Param("windowStart") LocalTime windowStart,
            @Param("windowEnd") LocalTime windowEnd
    );
}
