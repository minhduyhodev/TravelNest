package com.travelnest.auth.controller;

import com.travelnest.auth.dto.AuthResponse;
import com.travelnest.auth.dto.ChangePasswordRequest;
import com.travelnest.auth.dto.ForgotPasswordRequest;
import com.travelnest.auth.dto.ForgotPasswordResponse;
import com.travelnest.auth.dto.LoginRequest;
import com.travelnest.auth.dto.RegisterRequest;
import com.travelnest.auth.dto.ResetPasswordRequest;
import com.travelnest.auth.dto.VerifyResetOtpRequest;
import com.travelnest.auth.dto.VerifyResetOtpResponse;
import com.travelnest.auth.service.AuthService;
import com.travelnest.common.api.ApiResponse;
import com.travelnest.security.AuthenticatedUser;
import com.travelnest.user.dto.UserProfileResponse;
import com.travelnest.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Registered successfully", authService.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Logged in successfully", authService.login(request)));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<ForgotPasswordResponse>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "If the email exists, a reset OTP has been issued",
                authService.forgotPassword(request)
        ));
    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<ApiResponse<VerifyResetOtpResponse>> verifyResetOtp(
            @Valid @RequestBody VerifyResetOtpRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Reset OTP verified successfully",
                authService.verifyResetOtp(request)
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> me(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(authService.me(authenticatedUser)));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(authenticatedUser, request);
        return ResponseEntity.ok(ApiResponse.success("Password updated successfully", null));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }
}
