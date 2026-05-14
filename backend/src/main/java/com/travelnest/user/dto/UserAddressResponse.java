package com.travelnest.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAddressResponse {

    private Long id;
    private String label;
    private String fullName;
    private String phone;
    private String addressLine;
    private String ward;
    private String district;
    private String province;

    @JsonProperty("isDefault")
    private boolean isDefault;
}
