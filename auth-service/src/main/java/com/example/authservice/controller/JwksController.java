package com.example.authservice.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Публичные ключи для проверки подписи токенов другими сервисами (resource server)
 *
 * @author Timur Khabibullin
 */
@Tag(name = "Auth")
@RestController
@RequiredArgsConstructor
public class JwksController {

    private final RSAKey rsaKey;

    @Operation(
            summary = "JWKS",
            description = "Набор публичных ключей (JWK Set) для проверки подписи JWT. Приватная часть ключа не отдаётся"
    )
    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        return new JWKSet(rsaKey.toPublicJWK()).toJSONObject();
    }
}
