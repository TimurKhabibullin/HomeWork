package com.example.taskservice.dto;

import com.example.taskservice.constants.TaskStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.time.Instant;

/**
 * Задача
 *
 * @author Timur Khabibullin
 */
@Getter
@Setter
@Accessors(chain = true)
@FieldNameConstants
public class TaskDto {

    /**
     * Идентификатор задачи
     */
    private Long id;

    /**
     * Наименование
     */
    private String title;

    /**
     * Описание
     */
    private String description;

    /**
     * Статус
     */
    private TaskStatus status;

    /**
     * Исполнитель (null, если не назначен)
     */
    private UserDto assignee;

    /**
     * Дата создания
     */
    private Instant createdAt;

    /**
     * Дата последнего изменения
     */
    private Instant updatedAt;
}
