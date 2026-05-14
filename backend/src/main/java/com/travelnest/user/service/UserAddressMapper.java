package com.travelnest.user.service;

import com.travelnest.user.dto.UserAddressResponse;
import com.travelnest.user.entity.UserAddressEntity;
import org.springframework.stereotype.Component;

@Component
public class UserAddressMapper {

    public UserAddressResponse toResponse(UserAddressEntity address) {
        return new UserAddressResponse(
                address.getId(),
                address.getLabel(),
                address.getFullName(),
                address.getPhone(),
                address.getAddressLine(),
                address.getWard(),
                address.getDistrict(),
                address.getProvince(),
                address.isDefault()
        );
    }
}
