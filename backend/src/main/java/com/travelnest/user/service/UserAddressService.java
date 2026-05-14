package com.travelnest.user.service;

import com.travelnest.common.exception.ResourceNotFoundException;
import com.travelnest.security.AuthenticatedUser;
import com.travelnest.user.dto.UserAddressRequest;
import com.travelnest.user.dto.UserAddressResponse;
import com.travelnest.user.entity.UserAddressEntity;
import com.travelnest.user.entity.UserEntity;
import com.travelnest.user.repository.UserAddressRepository;
import com.travelnest.user.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAddressService {

    private final UserAddressRepository userAddressRepository;
    private final UserRepository userRepository;
    private final UserAddressMapper userAddressMapper;

    public UserAddressService(
            UserAddressRepository userAddressRepository,
            UserRepository userRepository,
            UserAddressMapper userAddressMapper
    ) {
        this.userAddressRepository = userAddressRepository;
        this.userRepository = userRepository;
        this.userAddressMapper = userAddressMapper;
    }

    @Transactional(readOnly = true)
    public List<UserAddressResponse> getCurrentUserAddresses(AuthenticatedUser authenticatedUser) {
        return userAddressRepository.findAllByUserIdOrderByIsDefaultDescCreatedAtDesc(authenticatedUser.getUserId())
                .stream()
                .map(userAddressMapper::toResponse)
                .toList();
    }

    @Transactional
    public UserAddressResponse createCurrentUserAddress(
            AuthenticatedUser authenticatedUser,
            UserAddressRequest request
    ) {
        UserEntity user = requireUser(authenticatedUser.getUserId());

        UserAddressEntity address = new UserAddressEntity();
        address.setUser(user);
        applyRequest(address, request);

        boolean shouldBeDefault = Boolean.TRUE.equals(request.getDefaultAddress())
                || !userAddressRepository.existsByUserId(user.getId());
        address.setDefault(shouldBeDefault);

        UserAddressEntity savedAddress = userAddressRepository.save(address);
        if (savedAddress.isDefault()) {
            userAddressRepository.clearDefaultForOtherAddresses(user.getId(), savedAddress.getId());
        }

        return userAddressMapper.toResponse(savedAddress);
    }

    @Transactional
    public UserAddressResponse updateCurrentUserAddress(
            AuthenticatedUser authenticatedUser,
            Long addressId,
            UserAddressRequest request
    ) {
        UserEntity user = requireUser(authenticatedUser.getUserId());
        UserAddressEntity address = requireAddress(user.getId(), addressId);

        boolean wasDefault = address.isDefault();
        applyRequest(address, request);

        if (Boolean.TRUE.equals(request.getDefaultAddress())) {
            address.setDefault(true);
        } else if (Boolean.FALSE.equals(request.getDefaultAddress()) && wasDefault) {
            address.setDefault(false);
        }

        UserAddressEntity savedAddress = userAddressRepository.save(address);

        if (savedAddress.isDefault()) {
            userAddressRepository.clearDefaultForOtherAddresses(user.getId(), savedAddress.getId());
        } else if (wasDefault) {
            promoteAnotherAddressAsDefault(user.getId(), savedAddress.getId());
        }

        return userAddressMapper.toResponse(savedAddress);
    }

    @Transactional
    public UserAddressResponse setDefaultAddress(AuthenticatedUser authenticatedUser, Long addressId) {
        Long userId = authenticatedUser.getUserId();
        UserAddressEntity address = requireAddress(userId, addressId);
        address.setDefault(true);
        UserAddressEntity savedAddress = userAddressRepository.save(address);
        userAddressRepository.clearDefaultForOtherAddresses(userId, savedAddress.getId());
        return userAddressMapper.toResponse(savedAddress);
    }

    @Transactional
    public void deleteCurrentUserAddress(AuthenticatedUser authenticatedUser, Long addressId) {
        Long userId = authenticatedUser.getUserId();
        UserAddressEntity address = requireAddress(userId, addressId);
        boolean wasDefault = address.isDefault();
        userAddressRepository.delete(address);

        if (wasDefault) {
            promoteAnotherAddressAsDefault(userId, addressId);
        }
    }

    private void applyRequest(UserAddressEntity address, UserAddressRequest request) {
        address.setLabel(trimToNull(request.getLabel()));
        address.setFullName(request.getFullName().trim());
        address.setPhone(request.getPhone().trim());
        address.setAddressLine(request.getAddressLine().trim());
        address.setWard(trimToNull(request.getWard()));
        address.setDistrict(request.getDistrict().trim());
        address.setProvince(request.getProvince().trim());
    }

    private void promoteAnotherAddressAsDefault(Long userId, Long excludedAddressId) {
        userAddressRepository.findFirstByUserIdAndIdNotOrderByCreatedAtAsc(userId, excludedAddressId)
                .ifPresent(address -> {
                    address.setDefault(true);
                    userAddressRepository.save(address);
                    userAddressRepository.clearDefaultForOtherAddresses(userId, address.getId());
                });
    }

    private UserEntity requireUser(Long userId) {
        return userRepository.findById(userId)
                .filter(user -> !user.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserAddressEntity requireAddress(Long userId, Long addressId) {
        return userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
