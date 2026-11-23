package com.example.authservice.security;

import com.example.authservice.constants.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Аутентифицируемый пользователь для Spring Security
 *
 * @author Timur Khabibullin
 */
@Getter
@RequiredArgsConstructor
public class AuthUser implements UserDetails {

    private final Long userId;

    private final String login;

    private final String passwordHash;

    private final Role role;

    private final boolean enabled;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return login;
    }
}
