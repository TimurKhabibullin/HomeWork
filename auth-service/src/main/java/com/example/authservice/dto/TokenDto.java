package com.example.authservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Ответ на успешный вход (имена полей по RFC 6749)
 *
 * @author Timur Khabibullin
 */
@Getter
@RequiredArgsConstructor
public class TokenDto {

    /**
     * Access-токен (JWT)
     */
    @JsonProperty("access_token")
    private final String accessToken;

    /**
     * Тип токена, всегда Bearer
     */
    @JsonProperty("token_type")
    private final String tokenType;

    /**
     * Время жизни токена в секундах
     */
    @JsonProperty("expires_in")
    private final long expiresIn;
}
