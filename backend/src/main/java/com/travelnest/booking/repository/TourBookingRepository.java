package com.travelnest.booking.repository;

import com.travelnest.booking.entity.TourBookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TourBookingRepository extends JpaRepository<TourBookingEntity, Long> {

    @Query("""
            select coalesce(sum(tb.guestCount), 0)
            from TourBookingEntity tb
            where tb.tourSlot.id = :tourSlotId
              and tb.booking.status <> 'CANCELLED'
            """)
    Integer sumReservedGuests(@Param("tourSlotId") Long tourSlotId);
}
