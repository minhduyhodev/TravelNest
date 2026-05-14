package com.travelnest.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.travelnest.user.entity.RoleEntity;
import com.travelnest.user.entity.UserRole;
import com.travelnest.user.repository.RoleRepository;
import com.travelnest.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class DefaultAdminSeederTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private DefaultAdminProperties defaultAdminProperties;
    private DefaultAdminSeeder defaultAdminSeeder;

    @BeforeEach
    void setUp() {
        defaultAdminProperties = new DefaultAdminProperties();
        defaultAdminSeeder = new DefaultAdminSeeder(
                defaultAdminProperties,
                userRepository,
                roleRepository,
                passwordEncoder
        );
    }

    @Test
    void run_createsDefaultAdminWhenMissing() throws Exception {
        RoleEntity adminRole = new RoleEntity();
        adminRole.setName(UserRole.ADMIN.name());

        when(userRepository.existsByEmailIgnoreCase("admin@travelnest.local")).thenReturn(false);
        when(roleRepository.findByName(UserRole.ADMIN.name())).thenReturn(Optional.of(adminRole));

        defaultAdminSeeder.run(new DefaultApplicationArguments(new String[0]));

        verify(userRepository).save(any());
    }

    @Test
    void run_skipsWhenDefaultAdminAlreadyExists() throws Exception {
        when(userRepository.existsByEmailIgnoreCase("admin@travelnest.local")).thenReturn(true);

        defaultAdminSeeder.run(new DefaultApplicationArguments(new String[0]));

        verify(roleRepository, never()).findByName(any());
        verify(userRepository, never()).save(any());
    }
}
