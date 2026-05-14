package com.travelnest.user.service;

import com.travelnest.user.dto.UserProfileResponse;
import com.travelnest.user.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserProfileResponse toResponse(UserEntity user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.getDateOfBirth(),
                user.getGender() == null ? null : user.getGender().name(),
                user.getRole().name(),
                user.getStatus().name(),
                user.isEmailVerified(),
                user.getPreferredLang()
        );
    }
}
