package com.travelnest.booking.repository;

import com.travelnest.booking.entity.BookingEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    boolean existsByOrderItemId(Long orderItemId);

    @EntityGraph(attributePaths = {"order", "orderItem", "hotelBooking", "tourBooking", "restaurantBooking"})
    List<BookingEntity> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"order", "orderItem", "hotelBooking", "tourBooking", "restaurantBooking"})
    Optional<BookingEntity> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {"order", "orderItem", "hotelBooking", "tourBooking", "restaurantBooking"})
    Optional<BookingEntity> findById(Long id);

    @EntityGraph(attributePaths = {"order", "orderItem", "hotelBooking", "tourBooking", "restaurantBooking"})
    @Query("""
            select b
            from BookingEntity b
            where (:status is null or upper(b.status) = upper(:status))
              and (:serviceType is null or upper(b.serviceType) = upper(:serviceType))
              and (:serviceDate is null or b.orderItem.startDate = :serviceDate)
            order by b.createdAt desc
            """)
    List<BookingEntity> searchManagementBookings(
            @Param("status") String status,
            @Param("serviceType") String serviceType,
            @Param("serviceDate") LocalDate serviceDate
    );
}
