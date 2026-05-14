package com.travelnest.user.service;

import com.travelnest.auth.dto.ChangePasswordRequest;
import com.travelnest.common.exception.BadRequestException;
import com.travelnest.common.exception.ResourceNotFoundException;
import com.travelnest.security.AuthenticatedUser;
import com.travelnest.user.dto.AdminCreateStaffRequest;
import com.travelnest.user.dto.UpdateProfileRequest;
import com.travelnest.user.dto.UpdateUserStatusRequest;
import com.travelnest.user.dto.UserProfileResponse;
import com.travelnest.user.entity.RoleEntity;
import com.travelnest.user.entity.UserEntity;
import com.travelnest.user.entity.UserRole;
import com.travelnest.user.entity.UserStatus;
import com.travelnest.user.repository.RoleRepository;
import com.travelnest.user.repository.UserRepository;
import java.util.LinkedHashSet;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(AuthenticatedUser authenticatedUser) {
        return userMapper.toResponse(requireUser(authenticatedUser.getUserId()));
    }

    @Transactional
    public UserProfileResponse updateCurrentUserProfile(
            AuthenticatedUser authenticatedUser,
            UpdateProfileRequest request
    ) {
        UserEntity user = requireUser(authenticatedUser.getUserId());
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl().trim());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getPreferredLang() != null && !request.getPreferredLang().isBlank()) {
            user.setPreferredLang(request.getPreferredLang().trim());
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void changePassword(AuthenticatedUser authenticatedUser, ChangePasswordRequest request) {
        UserEntity user = requireUser(authenticatedUser.getUserId());
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException("New password must be different from current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public UserProfileResponse createStaff(AdminCreateStaffRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BadRequestException("Email is already in use");
        }

        RoleEntity staffRole = roleRepository.findByName(UserRole.STAFF.name())
                .orElseThrow(() -> new ResourceNotFoundException("Default STAFF role is missing"));

        UserEntity user = new UserEntity();
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone());
        user.setRole(UserRole.STAFF);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);
        user.setPreferredLang("vi");
        user.setRoles(new LinkedHashSet<>());
        user.getRoles().add(staffRole);

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserProfileResponse updateUserStatus(Long userId, UpdateUserStatusRequest request) {
        UserEntity user = requireUser(userId);
        user.setStatus(request.getStatus());
        return userMapper.toResponse(userRepository.save(user));
    }

    private UserEntity requireUser(Long userId) {
        return userRepository.findById(userId)
                .filter(user -> !user.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
