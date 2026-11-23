package com.example.authservice.service;

import com.example.authservice.configuration.JwtConfig;
import com.example.authservice.configuration.JwtProperties;
import com.example.authservice.constants.Role;
import com.example.authservice.security.AuthUser;
import com.nimbusds.jose.jwk.RSAKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Timur Khabibullin
 */
class TokenServiceTest {

    private static final AuthUser USER = new AuthUser(42L, "alice", "hash", Role.USER, true);

    private JwtProperties props;

    private JwtConfig config;

    private RSAKey rsaKey;

    private TokenService service;

    private JwtDecoder decoder;

    @BeforeEach
    void setUp() throws Exception {
        props = new JwtProperties();
        props.setIssuer("http://issuer.test");
        props.setAccessTokenTtl(Duration.ofMinutes(5));

        config = new JwtConfig(props);
        rsaKey = config.rsaKey();
        service = new TokenService(config.jwtEncoder(rsaKey), props);
        decoder = config.jwtDecoder(rsaKey);
    }

    @Test
    void shouldIssueSignedTokenWithExpectedClaims() {
        var token = service.issue(USER);

        assertThat(token.getTokenType()).isEqualTo("Bearer");
        assertThat(token.getExpiresIn()).isEqualTo(300L);

        var jwt = decoder.decode(token.getAccessToken());

        assertThat(jwt.getSubject()).isEqualTo("42");
        assertThat(jwt.getIssuer()).hasToString("http://issuer.test");
        assertThat(jwt.<String>getClaim("login")).isEqualTo("alice");
        assertThat(jwt.<List<String>>getClaim("roles")).containsExactly("USER");
        assertThat(jwt.getHeaders()).containsEntry("kid", rsaKey.getKeyID());
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()).truncatedTo(ChronoUnit.SECONDS))
                .isEqualTo(Duration.ofMinutes(5));
    }

    @Test
    void shouldRejectTokenFromOtherIssuer() throws Exception {
        var otherProps = new JwtProperties();
        otherProps.setIssuer("http://other-issuer.test");
        var foreign = new TokenService(config.jwtEncoder(rsaKey), otherProps).issue(USER);

        assertThatThrownBy(() -> decoder.decode(foreign.getAccessToken())).isInstanceOf(JwtException.class);
    }

    @Test
    void shouldRejectTokenSignedWithOtherKey() throws Exception {
        var otherConfig = new JwtConfig(props);
        var foreign = new TokenService(otherConfig.jwtEncoder(otherConfig.rsaKey()), props).issue(USER);

        assertThatThrownBy(() -> decoder.decode(foreign.getAccessToken())).isInstanceOf(JwtException.class);
    }

    @Test
    void shouldRejectExpiredToken() {
        var now = Instant.now();
        var claims = JwtClaimsSet.builder()
                                 .issuer(props.getIssuer())
                                 .subject("42")
                                 .issuedAt(now.minus(2, ChronoUnit.HOURS))
                                 .expiresAt(now.minus(1, ChronoUnit.HOURS))
                                 .build();
        var header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        var expired = config.jwtEncoder(rsaKey).encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        assertThatThrownBy(() -> decoder.decode(expired)).isInstanceOf(JwtException.class);
    }
}
