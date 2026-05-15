package com.travelnest.booking.repository;

import com.travelnest.booking.entity.BookingEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    boolean existsByOrderItemId(Long orderItemId);

    @EntityGraph(attributePaths = {"order", "orderItem", "hotelBooking", "tourBooking", "restaurantBooking"})
    List<BookingEntity> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"order", "orderItem", "hotelBooking", "tourBooking", "restaurantBooking"})
    Optional<BookingEntity> findByIdAndUserId(Long id, Long userId);
}
