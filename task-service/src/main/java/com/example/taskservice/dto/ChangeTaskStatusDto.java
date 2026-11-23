package com.example.taskservice.dto;

import com.example.taskservice.constants.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

/**
 * Запрос на смену статуса задачи
 *
 * @author Timur Khabibullin
 */
@Getter
@Setter
@Accessors(chain = true)
@FieldNameConstants
public class ChangeTaskStatusDto {

    /**
     * Новый статус
     */
    @NotNull
    private TaskStatus status;
}
