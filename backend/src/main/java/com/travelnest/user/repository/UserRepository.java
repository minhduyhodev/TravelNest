package com.travelnest.user.repository;

import com.travelnest.user.entity.UserEntity;
import com.travelnest.user.entity.UserRole;
import com.travelnest.user.entity.UserStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<UserEntity> findByEmailAndIsDeletedFalse(String email);

    List<UserEntity> findAllByRoleAndIsDeletedFalseOrderByCreatedAtDesc(UserRole role);

    List<UserEntity> findAllByRoleAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(UserRole role, UserStatus status);
}
