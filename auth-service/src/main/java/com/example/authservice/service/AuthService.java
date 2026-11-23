package com.example.authservice.service;

import com.example.authservice.dto.LoginDto;
import com.example.authservice.dto.ProfileDto;
import com.example.authservice.dto.RegisterDto;
import com.example.authservice.dto.TokenDto;
import com.example.authservice.entity.Credential;
import com.example.authservice.entity.User;
import com.example.authservice.exception.ConflictException;
import com.example.authservice.exception.NotFoundException;
import com.example.authservice.repository.CredentialRepository;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.security.AuthUser;
import com.example.authservice.transformer.CredentialTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Регистрация, вход по логину и паролю, профиль текущего пользователя
 *
 * @author Timur Khabibullin
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;

    private final CredentialRepository credentialRepo;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final TokenService tokenService;

    private final CredentialTransformer t;

    /**
     * Создаёт пользователя в общей таблице users и его учётные данные. Логин и почта хранятся в нижнем регистре.
     * Почта уже существующего пользователя (например, созданного task-service) занята: присвоить чужую запись регистрацией нельзя.
     */
    @Transactional
    public ProfileDto register(RegisterDto dto) {
        var login = normalize(dto.getLogin());
        var email = normalize(dto.getEmail());

        if (credentialRepo.existsByLogin(login)) {
            throw new ConflictException("Логин уже занят");
        }
        if (userRepo.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Пользователь с такой почтой уже существует");
        }

        var user = userRepo.saveAndFlush(new User()
                .setName(dto.getName().trim())
                .setEmail(email));
        var credential = credentialRepo.saveAndFlush(new Credential()
                .setUser(user)
                .setLogin(login)
                .setPasswordHash(passwordEncoder.encode(dto.getPassword())));

        return t.toProfile(credential);
    }

    /**
     * Проверяет логин и пароль средствами Spring Security и выпускает токен.
     * При неверных данных выбрасывается AuthenticationException (HTTP 401), причина (нет логина или не тот пароль) не раскрывается.
     */
    public TokenDto login(LoginDto dto) {
        var authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(normalize(dto.getLogin()), dto.getPassword()));

        return tokenService.issue((AuthUser) authentication.getPrincipal());
    }

    @Transactional(readOnly = true)
    public ProfileDto getProfile(Long userId) {
        return credentialRepo.findById(userId)
                             .map(t::toProfile)
                             .orElseThrow(() -> new NotFoundException("Пользователь %d не найден".formatted(userId)));
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
