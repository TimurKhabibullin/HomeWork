package com.example.authservice.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционный тест API авторизации: PostgreSQL поднимается через Testcontainers
 *
 * @author Timur Khabibullin
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthApiIT {

    private static final String PASSWORD = "correct-horse-battery";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void shouldRegisterLoginAndReadProfile() throws Exception {
        var login = uniqueLogin();

        register(login, login + "@example.com", PASSWORD)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.login").value(login))
                .andExpect(jsonPath("$.email").value(login + "@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        var token = login(login, PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.expires_in").value(3600))
                .andReturn();
        var accessToken = json(token.getResponse().getContentAsString()).get("access_token").asText();

        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + accessToken))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.login").value(login))
           .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void shouldStoreOnlyPasswordHashInSharedUsersDatabase() throws Exception {
        var login = uniqueLogin();
        register(login, login + "@example.com", PASSWORD).andExpect(status().isCreated());

        var hash = jdbc.queryForObject("select password_hash from credentials where login = ?", String.class, login);
        var userRows = jdbc.queryForObject("select count(*) from users where email = ?", Integer.class, login + "@example.com");

        assertThat(hash).startsWith("$2").isNotEqualTo(PASSWORD);
        assertThat(userRows).isEqualTo(1);
    }

    @Test
    void shouldIssueTokenVerifiableByPublishedJwks() throws Exception {
        var login = uniqueLogin();
        register(login, login + "@example.com", PASSWORD).andExpect(status().isCreated());
        var response = login(login, PASSWORD).andReturn().getResponse().getContentAsString();
        var accessToken = json(response).get("access_token").asText();

        var jwksJson = mvc.perform(get("/.well-known/jwks.json"))
                          .andExpect(status().isOk())
                          .andReturn()
                          .getResponse()
                          .getContentAsString();
        var jwk = JWKSet.parse(jwksJson).getKeys().get(0);
        var signed = SignedJWT.parse(accessToken);

        assertThat(jwk.isPrivate()).isFalse();
        assertThat(jwksJson).doesNotContain("\"d\"");
        assertThat(signed.getHeader().getKeyID()).isEqualTo(jwk.getKeyID());
        assertThat(signed.verify(new RSASSAVerifier((RSAKey) jwk))).isTrue();
    }

    @Test
    void shouldTreatLoginCaseInsensitively() throws Exception {
        var login = uniqueLogin();
        register(login, login + "@example.com", PASSWORD).andExpect(status().isCreated());

        login(login.toUpperCase(), PASSWORD).andExpect(status().isOk());
        register(login.toUpperCase(), "other-" + login + "@example.com", PASSWORD).andExpect(status().isConflict());
    }

    @Test
    void shouldRejectDuplicateEmailIgnoringCase() throws Exception {
        var login = uniqueLogin();
        register(login, login + "@example.com", PASSWORD).andExpect(status().isCreated());

        register(uniqueLogin(), (login + "@EXAMPLE.com").toUpperCase(), PASSWORD).andExpect(status().isConflict());
    }

    @Test
    void shouldReturnSameUnauthorizedForWrongPasswordAndUnknownLogin() throws Exception {
        var login = uniqueLogin();
        register(login, login + "@example.com", PASSWORD).andExpect(status().isCreated());

        var wrongPassword = login(login, "wrong-password-123")
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();
        var unknownLogin = login("nobody-" + UUID.randomUUID().toString().substring(0, 8), PASSWORD)
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        assertThat(json(wrongPassword).get("detail")).isEqualTo(json(unknownLogin).get("detail"));
        assertThat(wrongPassword).doesNotContain(login);
    }

    @Test
    void shouldRejectInvalidRegistrationData() throws Exception {
        register("ab", "bad", "short")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.login").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void shouldProtectProfileEndpointWithBearerToken() throws Exception {
        mvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer not-a-jwt")).andExpect(status().isUnauthorized());
    }

    private ResultActions register(String login, String email, String password) throws Exception {
        var body = objectMapper.createObjectNode()
                               .put("login", login)
                               .put("password", password)
                               .put("name", "Test User")
                               .put("email", email);

        return mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body.toString()));
    }

    private ResultActions login(String login, String password) throws Exception {
        var body = objectMapper.createObjectNode().put("login", login).put("password", password);

        return mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(body.toString()));
    }

    private JsonNode json(String content) throws Exception {
        return objectMapper.readTree(content);
    }

    private static String uniqueLogin() {
        return "user-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
