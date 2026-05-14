package com.travelnest.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.travelnest.user.dto.UserProfileResponse;
import com.travelnest.user.entity.UserEntity;
import com.travelnest.user.entity.UserRole;
import com.travelnest.user.entity.UserStatus;
import com.travelnest.user.repository.RoleRepository;
import com.travelnest.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserMapper userMapper = new UserMapper();

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, roleRepository, passwordEncoder, userMapper);
    }

    @Test
    void getStaffAccounts_returnsFilteredStaffListWhenStatusProvided() {
        UserEntity staff = new UserEntity();
        staff.setId(21L);
        staff.setEmail("staff@travelnest.test");
        staff.setFullName("Support Staff");
        staff.setRole(UserRole.STAFF);
        staff.setStatus(UserStatus.ACTIVE);
        staff.setPreferredLang("vi");

        when(userRepository.findAllByRoleAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(UserRole.STAFF, UserStatus.ACTIVE))
                .thenReturn(List.of(staff));

        List<UserProfileResponse> response = userService.getStaffAccounts(UserStatus.ACTIVE);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().getRole()).isEqualTo("STAFF");
        assertThat(response.getFirst().getStatus()).isEqualTo("ACTIVE");
        assertThat(response.getFirst().getEmail()).isEqualTo("staff@travelnest.test");
    }

    @Test
    void getStaffAccounts_returnsAllStaffWhenStatusMissing() {
        when(userRepository.findAllByRoleAndIsDeletedFalseOrderByCreatedAtDesc(UserRole.STAFF))
                .thenReturn(List.of());

        List<UserProfileResponse> response = userService.getStaffAccounts(null);

        assertThat(response).isEmpty();
    }
}
