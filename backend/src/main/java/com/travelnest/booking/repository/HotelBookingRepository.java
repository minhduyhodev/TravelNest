package com.travelnest.booking.repository;

import com.travelnest.booking.entity.HotelBookingEntity;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HotelBookingRepository extends JpaRepository<HotelBookingEntity, Long> {

    @Query("""
            select coalesce(sum(hb.numRooms), 0)
            from HotelBookingEntity hb
            where hb.hotel.id = :hotelId
              and (:roomTypeId is null or hb.roomType.id = :roomTypeId)
              and hb.booking.status <> 'CANCELLED'
              and hb.checkInDate < :checkOutDate
              and hb.checkOutDate > :checkInDate
            """)
    Integer sumReservedRooms(
            @Param("hotelId") Long hotelId,
            @Param("roomTypeId") Long roomTypeId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );
}
