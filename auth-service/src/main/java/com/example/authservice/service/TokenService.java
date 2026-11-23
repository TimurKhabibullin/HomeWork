package com.example.authservice.service;

import com.example.authservice.configuration.JwtProperties;
import com.example.authservice.dto.TokenDto;
import com.example.authservice.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Выпуск access-токенов (JWT, RS256)
 *
 * @author Timur Khabibullin
 */
@Service
@RequiredArgsConstructor
public class TokenService {

    private static final String TOKEN_TYPE = "Bearer";

    private final JwtEncoder encoder;

    private final JwtProperties props;

    public TokenDto issue(AuthUser user) {
        var now = Instant.now();
        var ttl = props.getAccessTokenTtl();
        var claims = JwtClaimsSet.builder()
                                 .issuer(props.getIssuer())
                                 .subject(String.valueOf(user.getUserId()))
                                 .issuedAt(now)
                                 .expiresAt(now.plus(ttl))
                                 .claim("login", user.getLogin())
                                 .claim("roles", List.of(user.getRole().name()))
                                 .build();
        var header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        var token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new TokenDto(token, TOKEN_TYPE, ttl.toSeconds());
    }
}
