package com.travelnest.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.travelnest.security.AuthenticatedUser;
import com.travelnest.user.dto.UserAddressRequest;
import com.travelnest.user.dto.UserAddressResponse;
import com.travelnest.user.entity.UserAddressEntity;
import com.travelnest.user.entity.UserEntity;
import com.travelnest.user.repository.UserAddressRepository;
import com.travelnest.user.repository.UserRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class UserAddressServiceTest {

    @Mock
    private UserAddressRepository userAddressRepository;

    @Mock
    private UserRepository userRepository;

    private final UserAddressMapper userAddressMapper = new UserAddressMapper();

    private UserAddressService userAddressService;
    private AuthenticatedUser authenticatedUser;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        userAddressService = new UserAddressService(userAddressRepository, userRepository, userAddressMapper);
        authenticatedUser = new AuthenticatedUser(
                7L,
                "customer@travelnest.test",
                "secret",
                "Travel Customer",
                "CUSTOMER",
                Set.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );

        user = new UserEntity();
        user.setId(7L);
        user.setEmail("customer@travelnest.test");
        user.setFullName("Travel Customer");
    }

    @Test
    void createCurrentUserAddress_marksFirstAddressAsDefault() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(userAddressRepository.existsByUserId(7L)).thenReturn(false);
        when(userAddressRepository.save(any(UserAddressEntity.class))).thenAnswer(invocation -> {
            UserAddressEntity entity = invocation.getArgument(0);
            entity.setId(101L);
            return entity;
        });

        UserAddressRequest request = new UserAddressRequest(
                "Home",
                "Travel Customer",
                "0901234567",
                "123 Nguyen Hue",
                "Ben Nghe",
                "District 1",
                "Ho Chi Minh City",
                false
        );

        UserAddressResponse response = userAddressService.createCurrentUserAddress(authenticatedUser, request);

        assertThat(response.isDefault()).isTrue();
        verify(userAddressRepository).clearDefaultForOtherAddresses(7L, 101L);
    }

    @Test
    void updateCurrentUserAddress_promotesAnotherAddressWhenCurrentDefaultIsUnset() {
        UserAddressEntity currentDefault = new UserAddressEntity();
        currentDefault.setId(10L);
        currentDefault.setUser(user);
        currentDefault.setDefault(true);
        currentDefault.setFullName("Travel Customer");
        currentDefault.setPhone("0901234567");
        currentDefault.setAddressLine("123 Nguyen Hue");
        currentDefault.setDistrict("District 1");
        currentDefault.setProvince("Ho Chi Minh City");

        UserAddressEntity fallback = new UserAddressEntity();
        fallback.setId(11L);
        fallback.setUser(user);
        fallback.setFullName("Travel Customer");
        fallback.setPhone("0901234568");
        fallback.setAddressLine("25 Le Loi");
        fallback.setDistrict("District 1");
        fallback.setProvince("Ho Chi Minh City");

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(userAddressRepository.findByIdAndUserId(10L, 7L)).thenReturn(Optional.of(currentDefault));
        when(userAddressRepository.findFirstByUserIdAndIdNotOrderByCreatedAtAsc(7L, 10L))
                .thenReturn(Optional.of(fallback));
        when(userAddressRepository.save(any(UserAddressEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserAddressRequest request = new UserAddressRequest(
                "Office",
                "Travel Customer",
                "0901234567",
                "456 Hai Ba Trung",
                null,
                "District 3",
                "Ho Chi Minh City",
                false
        );

        UserAddressResponse response = userAddressService.updateCurrentUserAddress(authenticatedUser, 10L, request);

        assertThat(response.isDefault()).isFalse();
        assertThat(fallback.isDefault()).isTrue();
        verify(userAddressRepository).clearDefaultForOtherAddresses(7L, 11L);
    }

    @Test
    void deleteCurrentUserAddress_doesNotPromoteAnotherAddressWhenDeletedAddressWasNotDefault() {
        UserAddressEntity nonDefault = new UserAddressEntity();
        nonDefault.setId(10L);
        nonDefault.setUser(user);
        nonDefault.setDefault(false);

        when(userAddressRepository.findByIdAndUserId(10L, 7L)).thenReturn(Optional.of(nonDefault));

        userAddressService.deleteCurrentUserAddress(authenticatedUser, 10L);

        verify(userAddressRepository).delete(nonDefault);
        verify(userAddressRepository, never()).findFirstByUserIdAndIdNotOrderByCreatedAtAsc(any(), any());
    }
}
