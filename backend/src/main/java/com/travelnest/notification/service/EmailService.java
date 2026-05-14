package com.travelnest.notification.service;

public interface EmailService {

    void sendResetPasswordOtp(String recipientEmail, String recipientName, String otp, int expiresInMinutes);
}
