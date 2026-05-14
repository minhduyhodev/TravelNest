package com.travelnest.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.auth")
public class AuthFlowProperties {

    private int resetOtpExpirationMinutes = 15;
    private boolean debugReturnOtp = true;

    public int getResetOtpExpirationMinutes() {
        return resetOtpExpirationMinutes;
    }

    public void setResetOtpExpirationMinutes(int resetOtpExpirationMinutes) {
        this.resetOtpExpirationMinutes = resetOtpExpirationMinutes;
    }

    public boolean isDebugReturnOtp() {
        return debugReturnOtp;
    }

    public void setDebugReturnOtp(boolean debugReturnOtp) {
        this.debugReturnOtp = debugReturnOtp;
    }
}
