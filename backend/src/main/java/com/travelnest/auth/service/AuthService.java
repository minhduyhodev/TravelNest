package com.travelnest.auth.service;

import com.travelnest.auth.dto.AuthResponse;
import com.travelnest.auth.dto.ForgotPasswordRequest;
import com.travelnest.auth.dto.ForgotPasswordResponse;
import com.travelnest.auth.dto.LoginRequest;
import com.travelnest.auth.dto.RegisterRequest;
import com.travelnest.auth.dto.ResetPasswordRequest;
import com.travelnest.auth.dto.VerifyResetOtpRequest;
import com.travelnest.auth.dto.VerifyResetOtpResponse;
import com.travelnest.config.AuthFlowProperties;
import com.travelnest.common.exception.BadRequestException;
import com.travelnest.common.exception.ResourceNotFoundException;
import com.travelnest.notification.service.EmailService;
import com.travelnest.security.AuthenticatedUser;
import com.travelnest.security.JwtService;
import com.travelnest.user.dto.UserProfileResponse;
import com.travelnest.user.entity.OtpTokenEntity;
import com.travelnest.user.entity.RoleEntity;
import com.travelnest.user.entity.UserEntity;
import com.travelnest.user.entity.UserRole;
import com.travelnest.user.entity.UserStatus;
import com.travelnest.user.repository.OtpTokenRepository;
import com.travelnest.user.repository.RoleRepository;
import com.travelnest.user.repository.UserRepository;
import com.travelnest.user.service.UserMapper;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final String RESET_PASSWORD_OTP_TYPE = "RESET_PASSWORD";
    private static final String RESET_PASSWORD_SESSION_TYPE = "RESET_PASSWORD_SESSION";

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthFlowProperties authFlowProperties;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserRepository userRepository,
            RoleRepository roleRepository,
            OtpTokenRepository otpTokenRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper,
            AuthFlowProperties authFlowProperties,
            EmailService emailService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.otpTokenRepository = otpTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.authFlowProperties = authFlowProperties;
        this.emailService = emailService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BadRequestException("Email is already in use");
        }

        RoleEntity customerRole = roleRepository.findByName(UserRole.CUSTOMER.name())
                .orElseThrow(() -> new ResourceNotFoundException("Default CUSTOMER role is missing"));

        UserEntity user = new UserEntity();
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone());
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(false);
        user.setPreferredLang(request.getPreferredLang() == null || request.getPreferredLang().isBlank()
                ? "vi"
                : request.getPreferredLang().trim());
        user.setRoles(new LinkedHashSet<>());
        user.getRoles().add(customerRole);

        UserEntity savedUser = userRepository.save(user);
        return buildAuthResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        UserEntity user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() == UserStatus.BANNED || user.getStatus() == UserStatus.INACTIVE) {
            throw new BadRequestException("This account is not allowed to sign in");
        }

        return buildAuthResponse(user);
    }

    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        otpTokenRepository.deleteAllByExpiresAtBefore(LocalDateTime.now());

        return userRepository.findByEmailAndIsDeletedFalse(email)
                .map(user -> issueResetPasswordOtp(user, email))
                .orElseGet(() -> new ForgotPasswordResponse(email, null, authFlowProperties.getResetOtpExpirationMinutes()));
    }

    @Transactional
    public VerifyResetOtpResponse verifyResetOtp(VerifyResetOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String otp = request.getOtp().trim();

        UserEntity user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new BadRequestException("Reset OTP is invalid or expired"));

        OtpTokenEntity otpToken = getActiveToken(user, RESET_PASSWORD_OTP_TYPE, otp, "Reset OTP is invalid or expired");
        otpToken.setUsed(true);
        otpTokenRepository.save(otpToken);

        invalidateTokens(user, RESET_PASSWORD_SESSION_TYPE, null);
        return issueResetPasswordSession(user, email);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String resetToken = request.getResetToken().trim();

        UserEntity user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new BadRequestException("Reset session is invalid or expired"));

        OtpTokenEntity sessionToken = getActiveToken(
                user,
                RESET_PASSWORD_SESSION_TYPE,
                resetToken,
                "Reset session is invalid or expired"
        );

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException("New password must be different from current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        sessionToken.setUsed(true);
        userRepository.save(user);
        otpTokenRepository.save(sessionToken);

        invalidateTokens(user, RESET_PASSWORD_SESSION_TYPE, sessionToken.getId());
    }

    public UserProfileResponse me(AuthenticatedUser authenticatedUser) {
        UserEntity user = userRepository.findById(authenticatedUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toResponse(user);
    }

    private AuthResponse buildAuthResponse(UserEntity user) {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getFullName(),
                user.getRole().name(),
                java.util.Set.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(
                        "ROLE_" + user.getRole().name()
                ))
        );

        return new AuthResponse(
                jwtService.generateAccessToken(authenticatedUser),
                jwtService.generateRefreshToken(authenticatedUser),
                userMapper.toResponse(user)
        );
    }

    private ForgotPasswordResponse issueResetPasswordOtp(UserEntity user, String email) {
        invalidateTokens(user, RESET_PASSWORD_OTP_TYPE, null);
        invalidateTokens(user, RESET_PASSWORD_SESSION_TYPE, null);

        String otp = generateOtp();
        OtpTokenEntity otpToken = new OtpTokenEntity();
        otpToken.setUser(user);
        otpToken.setType(RESET_PASSWORD_OTP_TYPE);
        otpToken.setToken(otp);
        otpToken.setExpiresAt(LocalDateTime.now().plusMinutes(authFlowProperties.getResetOtpExpirationMinutes()));
        otpToken.setUsed(false);
        otpTokenRepository.save(otpToken);

        emailService.sendResetPasswordOtp(
                email,
                user.getFullName(),
                otp,
                authFlowProperties.getResetOtpExpirationMinutes()
        );

        if (authFlowProperties.isDebugReturnOtp()) {
            log.info("Generated reset OTP {} for {}", otp, email);
        } else {
            log.info("Generated reset OTP for {}", email);
        }

        return new ForgotPasswordResponse(
                email,
                authFlowProperties.isDebugReturnOtp() ? otp : null,
                authFlowProperties.getResetOtpExpirationMinutes()
        );
    }

    private VerifyResetOtpResponse issueResetPasswordSession(UserEntity user, String email) {
        String resetToken = generateSessionToken();
        OtpTokenEntity sessionToken = new OtpTokenEntity();
        sessionToken.setUser(user);
        sessionToken.setType(RESET_PASSWORD_SESSION_TYPE);
        sessionToken.setToken(resetToken);
        sessionToken.setExpiresAt(LocalDateTime.now().plusMinutes(authFlowProperties.getResetOtpExpirationMinutes()));
        sessionToken.setUsed(false);
        otpTokenRepository.save(sessionToken);

        return new VerifyResetOtpResponse(
                email,
                resetToken,
                authFlowProperties.getResetOtpExpirationMinutes()
        );
    }

    private void invalidateTokens(UserEntity user, String type, Long excludedTokenId) {
        otpTokenRepository.findAllByUserAndTypeAndIsUsedFalse(user, type)
                .stream()
                .filter(token -> excludedTokenId == null || !token.getId().equals(excludedTokenId))
                .forEach(token -> token.setUsed(true));
    }

    private OtpTokenEntity getActiveToken(UserEntity user, String type, String token, String invalidMessage) {
        OtpTokenEntity foundToken = otpTokenRepository
                .findFirstByUserAndTypeAndTokenAndIsUsedFalseOrderByIdDesc(user, type, token)
                .orElseThrow(() -> new BadRequestException(invalidMessage));

        if (foundToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            foundToken.setUsed(true);
            otpTokenRepository.save(foundToken);
            throw new BadRequestException(invalidMessage);
        }

        return foundToken;
    }

    private String generateOtp() {
        int value = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(value);
    }

    private String generateSessionToken() {
        final String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder builder = new StringBuilder(10);
        for (int index = 0; index < 10; index++) {
            builder.append(alphabet.charAt(secureRandom.nextInt(alphabet.length())));
        }
        return builder.toString();
    }
}
