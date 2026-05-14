package com.travelnest.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.travelnest.auth.dto.ForgotPasswordRequest;
import com.travelnest.auth.dto.ForgotPasswordResponse;
import com.travelnest.auth.dto.ResetPasswordRequest;
import com.travelnest.auth.dto.VerifyResetOtpRequest;
import com.travelnest.auth.dto.VerifyResetOtpResponse;
import com.travelnest.common.exception.BadRequestException;
import com.travelnest.config.AuthFlowProperties;
import com.travelnest.notification.service.EmailService;
import com.travelnest.security.JwtService;
import com.travelnest.user.entity.OtpTokenEntity;
import com.travelnest.user.entity.UserEntity;
import com.travelnest.user.repository.OtpTokenRepository;
import com.travelnest.user.repository.RoleRepository;
import com.travelnest.user.repository.UserRepository;
import com.travelnest.user.service.UserMapper;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServicePasswordResetTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private OtpTokenRepository otpTokenRepository;

    @Mock
    private EmailService emailService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserMapper userMapper = new UserMapper();

    private AuthService authService;

    @BeforeEach
    void setUp() {
        AuthFlowProperties authFlowProperties = new AuthFlowProperties();
        authFlowProperties.setDebugReturnOtp(true);
        authFlowProperties.setResetOtpExpirationMinutes(15);

        authService = new AuthService(
                authenticationManager,
                jwtService,
                userRepository,
                roleRepository,
                otpTokenRepository,
                passwordEncoder,
                userMapper,
                authFlowProperties,
                emailService
        );
    }

    @Test
    void forgotPassword_returnsDebugOtpWhenUserExists() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setEmail("admin@travelnest.local");

        when(userRepository.findByEmailAndIsDeletedFalse("admin@travelnest.local"))
                .thenReturn(Optional.of(user));
        when(otpTokenRepository.findAllByUserAndTypeAndIsUsedFalse(user, "RESET_PASSWORD"))
                .thenReturn(java.util.List.of());
        when(otpTokenRepository.findAllByUserAndTypeAndIsUsedFalse(user, "RESET_PASSWORD_SESSION"))
                .thenReturn(java.util.List.of());

        ForgotPasswordResponse response = authService.forgotPassword(
                new ForgotPasswordRequest("admin@travelnest.local")
        );

        assertThat(response.getEmail()).isEqualTo("admin@travelnest.local");
        assertThat(response.getDebugOtp()).hasSize(6);
        verify(otpTokenRepository).save(any(OtpTokenEntity.class));
        verify(emailService).sendResetPasswordOtp(any(), any(), any(), eq(15));
    }

    @Test
    void forgotPassword_doesNotFailWhenEmailDoesNotExist() {
        when(userRepository.findByEmailAndIsDeletedFalse("missing@travelnest.local"))
                .thenReturn(Optional.empty());

        ForgotPasswordResponse response = authService.forgotPassword(
                new ForgotPasswordRequest("missing@travelnest.local")
        );

        assertThat(response.getDebugOtp()).isNull();
        verify(otpTokenRepository, never()).save(any());
        verify(emailService, never()).sendResetPasswordOtp(any(), any(), any(), any(Integer.class));
    }

    @Test
    void verifyResetOtp_returnsShortLivedResetTokenWhenOtpIsValid() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setEmail("admin@travelnest.local");
        user.setPasswordHash(passwordEncoder.encode("Admin@123"));

        when(userRepository.findByEmailAndIsDeletedFalse("admin@travelnest.local"))
                .thenReturn(Optional.of(user));

        OtpTokenEntity otpToken = new OtpTokenEntity();
        otpToken.setId(10L);
        otpToken.setUser(user);
        otpToken.setType("RESET_PASSWORD");
        otpToken.setToken("123456");
        otpToken.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otpToken.setUsed(false);

        when(otpTokenRepository.findFirstByUserAndTypeAndTokenAndIsUsedFalseOrderByIdDesc(user, "RESET_PASSWORD", "123456"))
                .thenReturn(Optional.of(otpToken));
        when(otpTokenRepository.findAllByUserAndTypeAndIsUsedFalse(user, "RESET_PASSWORD_SESSION"))
                .thenReturn(java.util.List.of());

        VerifyResetOtpResponse response = authService.verifyResetOtp(
                new VerifyResetOtpRequest("admin@travelnest.local", "123456")
        );

        assertThat(otpToken.isUsed()).isTrue();
        verify(otpTokenRepository).save(otpToken);
        assertThat(response.getEmail()).isEqualTo("admin@travelnest.local");
        assertThat(response.getResetToken()).hasSize(10);
    }

    @Test
    void verifyResetOtp_rejectsExpiredOtp() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setEmail("admin@travelnest.local");
        user.setPasswordHash(passwordEncoder.encode("Admin@123"));

        when(userRepository.findByEmailAndIsDeletedFalse("admin@travelnest.local"))
                .thenReturn(Optional.of(user));

        OtpTokenEntity otpToken = new OtpTokenEntity();
        otpToken.setId(10L);
        otpToken.setUser(user);
        otpToken.setType("RESET_PASSWORD");
        otpToken.setToken("123456");
        otpToken.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        otpToken.setUsed(false);

        when(otpTokenRepository.findFirstByUserAndTypeAndTokenAndIsUsedFalseOrderByIdDesc(user, "RESET_PASSWORD", "123456"))
                .thenReturn(Optional.of(otpToken));

        assertThatThrownBy(() ->
                authService.verifyResetOtp(new VerifyResetOtpRequest("admin@travelnest.local", "123456"))
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Reset OTP is invalid or expired");
    }

    @Test
    void resetPassword_updatesPasswordWhenResetSessionIsValid() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setEmail("admin@travelnest.local");
        user.setPasswordHash(passwordEncoder.encode("Admin@123"));

        OtpTokenEntity resetSession = new OtpTokenEntity();
        resetSession.setId(20L);
        resetSession.setUser(user);
        resetSession.setType("RESET_PASSWORD_SESSION");
        resetSession.setToken("ABCD2345EF");
        resetSession.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        resetSession.setUsed(false);

        when(userRepository.findByEmailAndIsDeletedFalse("admin@travelnest.local"))
                .thenReturn(Optional.of(user));
        when(otpTokenRepository.findFirstByUserAndTypeAndTokenAndIsUsedFalseOrderByIdDesc(
                user,
                "RESET_PASSWORD_SESSION",
                "ABCD2345EF"
        )).thenReturn(Optional.of(resetSession));
        when(otpTokenRepository.findAllByUserAndTypeAndIsUsedFalse(user, "RESET_PASSWORD_SESSION"))
                .thenReturn(java.util.List.of(resetSession));

        authService.resetPassword(new ResetPasswordRequest("admin@travelnest.local", "ABCD2345EF", "Admin@456"));

        assertThat(passwordEncoder.matches("Admin@456", user.getPasswordHash())).isTrue();
        assertThat(resetSession.isUsed()).isTrue();
        verify(userRepository).save(user);
        verify(otpTokenRepository).save(resetSession);
    }

    @Test
    void resetPassword_rejectsExpiredResetSession() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setEmail("admin@travelnest.local");
        user.setPasswordHash(passwordEncoder.encode("Admin@123"));

        OtpTokenEntity resetSession = new OtpTokenEntity();
        resetSession.setId(20L);
        resetSession.setUser(user);
        resetSession.setType("RESET_PASSWORD_SESSION");
        resetSession.setToken("ABCD2345EF");
        resetSession.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        resetSession.setUsed(false);

        when(userRepository.findByEmailAndIsDeletedFalse("admin@travelnest.local"))
                .thenReturn(Optional.of(user));
        when(otpTokenRepository.findFirstByUserAndTypeAndTokenAndIsUsedFalseOrderByIdDesc(
                user,
                "RESET_PASSWORD_SESSION",
                "ABCD2345EF"
        )).thenReturn(Optional.of(resetSession));

        assertThatThrownBy(() ->
                authService.resetPassword(new ResetPasswordRequest("admin@travelnest.local", "ABCD2345EF", "Admin@456"))
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Reset session is invalid or expired");
    }
}
