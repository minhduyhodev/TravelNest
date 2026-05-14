package com.travelnest.user.repository;

import com.travelnest.user.entity.UserAddressEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAddressRepository extends JpaRepository<UserAddressEntity, Long> {

    List<UserAddressEntity> findAllByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);

    Optional<UserAddressEntity> findByIdAndUserId(Long id, Long userId);

    Optional<UserAddressEntity> findFirstByUserIdOrderByCreatedAtAsc(Long userId);

    Optional<UserAddressEntity> findFirstByUserIdAndIdNotOrderByCreatedAtAsc(Long userId, Long id);

    boolean existsByUserId(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update UserAddressEntity address
            set address.isDefault = false
            where address.user.id = :userId
              and address.id <> :addressId
            """)
    void clearDefaultForOtherAddresses(@Param("userId") Long userId, @Param("addressId") Long addressId);
}
