package com.example.taskservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

/**
 * Запрос на создание задачи
 *
 * @author Timur Khabibullin
 */
@Getter
@Setter
@Accessors(chain = true)
@FieldNameConstants
public class CreateTaskDto {

    /**
     * Наименование
     */
    @NotBlank
    @Size(max = 255)
    private String title;

    /**
     * Описание
     */
    @Size(max = 10000)
    private String description;

    /**
     * Идентификатор исполнителя (необязательно)
     */
    private Long assigneeId;
}
