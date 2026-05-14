package com.travelnest.config;

import com.travelnest.user.entity.RoleEntity;
import com.travelnest.user.entity.UserEntity;
import com.travelnest.user.entity.UserRole;
import com.travelnest.user.entity.UserStatus;
import com.travelnest.user.repository.RoleRepository;
import com.travelnest.user.repository.UserRepository;
import java.util.LinkedHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DefaultAdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DefaultAdminSeeder.class);

    private final DefaultAdminProperties defaultAdminProperties;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DefaultAdminSeeder(
            DefaultAdminProperties defaultAdminProperties,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.defaultAdminProperties = defaultAdminProperties;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!defaultAdminProperties.isEnabled()) {
            return;
        }

        String normalizedEmail = normalize(defaultAdminProperties.getEmail(), "admin@travelnest.local").toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            return;
        }

        RoleEntity adminRole = roleRepository.findByName(UserRole.ADMIN.name())
                .orElse(null);
        if (adminRole == null) {
            log.warn("Skipping default admin seeding because ADMIN role is missing");
            return;
        }

        UserEntity adminUser = new UserEntity();
        adminUser.setEmail(normalizedEmail);
        adminUser.setPasswordHash(passwordEncoder.encode(normalize(defaultAdminProperties.getPassword(), "Admin@123")));
        adminUser.setFullName(normalize(defaultAdminProperties.getFullName(), "TravelNest Admin"));
        adminUser.setPhone(normalize(defaultAdminProperties.getPhone(), "0900000000"));
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser.setEmailVerified(true);
        adminUser.setPreferredLang(normalize(defaultAdminProperties.getPreferredLang(), "vi"));
        adminUser.setRoles(new LinkedHashSet<>());
        adminUser.getRoles().add(adminRole);

        userRepository.save(adminUser);
        log.info("Seeded default admin account: {}", normalizedEmail);
    }

    private String normalize(String value, String fallback) {
        if (value == null) {
            return fallback;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }
}
