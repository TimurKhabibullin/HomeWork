package com.example.taskservice.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

/**
 * Пользователь
 *
 * @author Timur Khabibullin
 */
@Getter
@Setter
@Accessors(chain = true)
@FieldNameConstants
public class UserDto {

    /**
     * Идентификатор пользователя
     */
    private Long id;

    /**
     * Имя
     */
    private String name;

    /**
     * Почта
     */
    private String email;
}
