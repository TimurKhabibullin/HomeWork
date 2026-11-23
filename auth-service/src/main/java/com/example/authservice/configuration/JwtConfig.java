package com.example.authservice.configuration;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.IOException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Ключи подписи JWT (RS256), кодировщик и декодер токенов
 *
 * @author Timur Khabibullin
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class JwtConfig {

    private static final int GENERATED_KEY_SIZE = 2048;

    private final JwtProperties props;

    /**
     * Ключ подписи: из PEM-файлов, если они заданы, иначе временная пара
     */
    @Bean
    public RSAKey rsaKey() throws IOException, JOSEException, NoSuchAlgorithmException {
        RSAPrivateKey privateKey;
        RSAPublicKey publicKey;

        if (props.getPrivateKey() != null && props.getPublicKey() != null) {
            try (var in = props.getPrivateKey().getInputStream()) {
                privateKey = RsaKeyConverters.pkcs8().convert(in);
            }
            try (var in = props.getPublicKey().getInputStream()) {
                publicKey = RsaKeyConverters.x509().convert(in);
            }
        } else {
            log.warn("Ключи подписи JWT не заданы (auth.jwt.private-key / auth.jwt.public-key): сгенерирована временная пара, "
                    + "токены перестанут действовать после перезапуска, а несколько экземпляров сервиса несовместимы");

            var generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(GENERATED_KEY_SIZE);
            var pair = generator.generateKeyPair();
            privateKey = (RSAPrivateKey) pair.getPrivate();
            publicKey = (RSAPublicKey) pair.getPublic();
        }

        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .keyIDFromThumbprint()
                .build();
    }

    @Bean
    public JwtEncoder jwtEncoder(RSAKey rsaKey) {
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey)));
    }

    @Bean
    public JwtDecoder jwtDecoder(RSAKey rsaKey) throws JOSEException {
        var decoder = NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey())
                                      .signatureAlgorithm(SignatureAlgorithm.RS256)
                                      .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(props.getIssuer()));

        return decoder;
    }
}
