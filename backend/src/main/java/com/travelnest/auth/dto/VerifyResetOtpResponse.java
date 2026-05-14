package com.travelnest.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerifyResetOtpResponse {

    private String email;
    private String resetToken;
    private int expiresInMinutes;
}
