package com.example.authservice.security;

import com.example.authservice.repository.CredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Загружает учётные данные по логину (регистр логина не учитывается)
 *
 * @author Timur Khabibullin
 */
@Service
@RequiredArgsConstructor
public class CredentialUserDetailsService implements UserDetailsService {

    private final CredentialRepository repo;

    @Override
    public UserDetails loadUserByUsername(String username) {
        return repo.findByLogin(username.toLowerCase(Locale.ROOT))
                   .map(c -> new AuthUser(c.getId(), c.getLogin(), c.getPasswordHash(), c.getRole(), c.isEnabled()))
                   .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }
}
