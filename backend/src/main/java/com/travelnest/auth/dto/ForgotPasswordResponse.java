package com.travelnest.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordResponse {

    private String email;
    private String debugOtp;
    private int expiresInMinutes;
}
