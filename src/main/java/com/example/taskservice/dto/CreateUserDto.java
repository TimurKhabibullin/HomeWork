package com.example.taskservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

/**
 * Запрос на создание пользователя (только для тестирования)
 *
 * @author Timur Khabibullin
 */
@Getter
@Setter
@Accessors(chain = true)
@FieldNameConstants
public class CreateUserDto {

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
}
