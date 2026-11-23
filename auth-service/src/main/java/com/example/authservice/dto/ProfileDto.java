package com.example.authservice.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

/**
 * Профиль пользователя
 *
 * @author Timur Khabibullin
 */
@Getter
@Setter
@Accessors(chain = true)
@FieldNameConstants
public class ProfileDto {

    /**
     * Идентификатор пользователя
     */
    private Long id;

    /**
     * Логин
     */
    private String login;

    /**
     * Имя
     */
    private String name;

    /**
     * Почта
     */
    private String email;
}
