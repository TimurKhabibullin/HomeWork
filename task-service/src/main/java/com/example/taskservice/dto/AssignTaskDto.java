package com.example.taskservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

/**
 * Запрос на назначение исполнителя задачи
 *
 * @author Timur Khabibullin
 */
@Getter
@Setter
@Accessors(chain = true)
@FieldNameConstants
public class AssignTaskDto {

    /**
     * Идентификатор исполнителя
     */
    @NotNull
    private Long userId;
}
