package com.example.authservice.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

import java.time.Duration;

/**
 * Настройки выпуска JWT (префикс {@code auth.jwt})
 *
 * @author Timur Khabibullin
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.jwt")
public class JwtProperties {

    /**
     * Издатель токенов (claim iss); проверяется при валидации
     */
    private String issuer = "http://localhost:8081";

    /**
     * Время жизни access-токена
     */
    private Duration accessTokenTtl = Duration.ofHours(1);

    /**
     * Приватный ключ подписи в PEM (PKCS#8). Если не задан, при старте генерируется временная пара ключей
     */
    private Resource privateKey;

    /**
     * Публичный ключ в PEM (X.509)
     */
    private Resource publicKey;
}
