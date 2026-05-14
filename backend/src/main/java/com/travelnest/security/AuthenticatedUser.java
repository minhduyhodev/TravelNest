package com.travelnest.security;

import java.io.Serial;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class AuthenticatedUser extends User {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long userId;
    private final String displayName;
    private final String role;

    public AuthenticatedUser(
            Long userId,
            String username,
            String password,
            String displayName,
            String role,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(username, password, authorities);
        this.userId = userId;
        this.displayName = displayName;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRole() {
        return role;
    }
}
