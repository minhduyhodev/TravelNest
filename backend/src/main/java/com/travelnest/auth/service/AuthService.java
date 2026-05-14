package com.travelnest.auth.service;

import com.travelnest.auth.dto.AuthResponse;
import com.travelnest.auth.dto.LoginRequest;
import com.travelnest.auth.dto.RegisterRequest;
import com.travelnest.common.exception.BadRequestException;
import com.travelnest.common.exception.ResourceNotFoundException;
import com.travelnest.security.AuthenticatedUser;
import com.travelnest.security.JwtService;
import com.travelnest.user.dto.UserProfileResponse;
import com.travelnest.user.entity.RoleEntity;
import com.travelnest.user.entity.UserEntity;
import com.travelnest.user.entity.UserRole;
import com.travelnest.user.entity.UserStatus;
import com.travelnest.user.repository.RoleRepository;
import com.travelnest.user.repository.UserRepository;
import com.travelnest.user.service.UserMapper;
import java.util.LinkedHashSet;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
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
}
