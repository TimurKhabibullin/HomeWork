package com.example.authservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.nio.charset.StandardCharsets;

/**
 * Запрос на регистрацию
 *
 * @author Timur Khabibullin
 */
@Getter
@Setter
@Accessors(chain = true)
@FieldNameConstants
public class RegisterDto {

    /**
     * Максимальная длина пароля в байтах: BCrypt не учитывает остальное
     */
    private static final int MAX_PASSWORD_BYTES = 72;

    /**
     * Логин: латиница, цифры, точка, дефис и подчёркивание (регистр не учитывается)
     */
    @NotBlank
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "допустимы латиница, цифры, '.', '_' и '-'")
    private String login;

    /**
     * Пароль
     */
    @NotBlank
    @Size(min = 8, max = MAX_PASSWORD_BYTES)
    private String password;

    /**
     * Имя
     */
    @NotBlank
    @Size(max = 255)
    private String name;

    /**
     * Почта
     */
    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    @JsonIgnore
    @AssertTrue(message = "пароль не должен превышать 72 байта в UTF-8")
    public boolean isPasswordWithinBcryptLimit() {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= MAX_PASSWORD_BYTES;
    }
}
