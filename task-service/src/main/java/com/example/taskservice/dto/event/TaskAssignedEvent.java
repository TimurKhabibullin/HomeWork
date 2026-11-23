package com.example.taskservice.dto.event;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.time.Instant;
import java.util.UUID;

/**
 * Событие назначения исполнителя задачи
 *
 * @param eventId            уникальный идентификатор события
 * @param occurredAt         время возникновения события
 * @param taskId             идентификатор задачи
 * @param assigneeId         идентификатор нового исполнителя
 * @param previousAssigneeId идентификатор предыдущего исполнителя (null, если не был назначен)
 * @author Timur Khabibullin
 */
@JsonTypeName(TaskAssignedEvent.TYPE)
public record TaskAssignedEvent(UUID eventId, Instant occurredAt, Long taskId, Long assigneeId, Long previousAssigneeId) implements TaskEvent {

    public static final String TYPE = "TASK_ASSIGNED";

    public static TaskAssignedEvent of(Long taskId, Long assigneeId, Long previousAssigneeId) {
        return new TaskAssignedEvent(UUID.randomUUID(), Instant.now(), taskId, assigneeId, previousAssigneeId);
    }
}
