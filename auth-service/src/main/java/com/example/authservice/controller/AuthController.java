package com.example.authservice.controller;

import com.example.authservice.configuration.OpenApiConfig;
import com.example.authservice.dto.LoginDto;
import com.example.authservice.dto.ProfileDto;
import com.example.authservice.dto.RegisterDto;
import com.example.authservice.dto.TokenDto;
import com.example.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Timur Khabibullin
 */
@Tag(name = "Auth")
@RequestMapping("/api/v1/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService svc;

    @Operation(
            summary = "Регистрация",
            description = "Создаёт пользователя и учётные данные. Логин (3–50 символов) и почта уникальны без учёта регистра, "
                    + "пароль 8–72 символа"
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/register")
    public ProfileDto register(@Valid @RequestBody RegisterDto body) {
        return svc.register(body);
    }

    @Operation(
            summary = "Вход по логину и паролю",
            description = "Возвращает Bearer JWT (RS256). При неверном логине или пароле — 401 без уточнения причины"
    )
    @PostMapping("/login")
    public TokenDto login(@Valid @RequestBody LoginDto body) {
        return svc.login(body);
    }

    @Operation(
            summary = "Текущий пользователь",
            description = "Профиль пользователя из Bearer-токена"
    )
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @GetMapping("/me")
    public ProfileDto me(@AuthenticationPrincipal Jwt jwt) {
        return svc.getProfile(Long.valueOf(jwt.getSubject()));
    }
}
