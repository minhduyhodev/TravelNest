package com.travelnest.security;

import com.travelnest.user.entity.RoleEntity;
import com.travelnest.user.entity.UserEntity;
import com.travelnest.user.repository.UserRepository;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public AuthenticatedUser loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmailAndIsDeletedFalse(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Set<String> roleNames = new LinkedHashSet<>();
        if (user.getRoles() != null) {
            roleNames.addAll(user.getRoles().stream().map(RoleEntity::getName).collect(Collectors.toSet()));
        }
        roleNames.add(user.getRole().name());

        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        for (String roleName : roleNames) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
        }
        if (user.getRoles() != null) {
            user.getRoles().stream()
                    .flatMap(role -> role.getPermissions().stream())
                    .forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission.getName())));
        }

        return new AuthenticatedUser(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getFullName(),
                user.getRole().name(),
                authorities
        );
    }
}
