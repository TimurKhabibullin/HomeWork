package com.example.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

/**
 * Запрос на вход по логину и паролю
 *
 * @author Timur Khabibullin
 */
@Getter
@Setter
@Accessors(chain = true)
@FieldNameConstants
public class LoginDto {

    /**
     * Логин
     */
    @NotBlank
    private String login;

    /**
     * Пароль
     */
    @NotBlank
    private String password;
}
