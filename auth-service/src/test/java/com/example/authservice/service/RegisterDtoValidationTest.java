package com.example.authservice.service;

import com.example.authservice.dto.RegisterDto;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Timur Khabibullin
 */
class RegisterDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldBeValidOnCorrectData() {
        assertThat(validator.validate(valid())).isEmpty();
    }

    @ValueSource(strings = {"ab", "has space", "кириллица", "semi;colon"})
    @NullSource
    @ParameterizedTest
    void shouldBeInvalidOnBadLogin(String login) {
        assertThat(validator.validate(valid().setLogin(login)))
                .extracting(v -> v.getPropertyPath().toString())
                .contains(RegisterDto.F.LOGIN);
    }

    @ValueSource(strings = {"short", "       "})
    @NullSource
    @ParameterizedTest
    void shouldBeInvalidOnBadPassword(String password) {
        assertThat(validator.validate(valid().setPassword(password)))
                .extracting(v -> v.getPropertyPath().toString())
                .contains(RegisterDto.F.PASSWORD);
    }

    @Test
    void shouldBeInvalidOnPasswordLongerThan72Bytes() {
        // 40 кириллических символов = 80 байт в UTF-8, при этом меньше 72 символов
        var password = "ы".repeat(40);

        assertThat(validator.validate(valid().setPassword(password)))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("passwordWithinBcryptLimit");
    }

    @CsvSource({"not-an-email", "a@", "@b.com"})
    @ParameterizedTest
    void shouldBeInvalidOnBadEmail(String email) {
        assertThat(validator.validate(valid().setEmail(email)))
                .extracting(v -> v.getPropertyPath().toString())
                .contains(RegisterDto.F.EMAIL);
    }

    private static RegisterDto valid() {
        return new RegisterDto()
                .setLogin("alice_1")
                .setPassword("correct-horse-battery")
                .setName("Alice")
                .setEmail("alice@example.com");
    }
}
