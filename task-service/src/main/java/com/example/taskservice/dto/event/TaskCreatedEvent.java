package com.example.taskservice.dto.event;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.time.Instant;
import java.util.UUID;

/**
 * Событие создания задачи
 *
 * @param eventId    уникальный идентификатор события
 * @param occurredAt время возникновения события
 * @param taskId     идентификатор задачи
 * @param title      наименование задачи
 * @param assigneeId идентификатор исполнителя, если он указан при создании
 * @author Timur Khabibullin
 */
@JsonTypeName(TaskCreatedEvent.TYPE)
public record TaskCreatedEvent(UUID eventId, Instant occurredAt, Long taskId, String title, Long assigneeId) implements TaskEvent {

    public static final String TYPE = "TASK_CREATED";

    public static TaskCreatedEvent of(Long taskId, String title, Long assigneeId) {
        return new TaskCreatedEvent(UUID.randomUUID(), Instant.now(), taskId, title, assigneeId);
    }
}
