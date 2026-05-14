package com.travelnest.user.repository;

import com.travelnest.user.entity.OtpTokenEntity;
import com.travelnest.user.entity.UserEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpTokenRepository extends JpaRepository<OtpTokenEntity, Long> {

    List<OtpTokenEntity> findAllByUserAndTypeAndIsUsedFalse(UserEntity user, String type);

    Optional<OtpTokenEntity> findFirstByUserAndTypeAndTokenAndIsUsedFalseOrderByIdDesc(
            UserEntity user,
            String type,
            String token
    );

    void deleteAllByExpiresAtBefore(LocalDateTime expiresAt);
}
