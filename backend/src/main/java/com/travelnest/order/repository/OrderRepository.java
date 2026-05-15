package com.travelnest.order.repository;

import com.travelnest.order.entity.OrderEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @EntityGraph(attributePaths = {"items"})
    List<OrderEntity> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"items"})
    Optional<OrderEntity> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {"items"})
    Optional<OrderEntity> findById(Long id);
}
