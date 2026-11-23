package com.example.taskservice.dto.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;
import java.util.UUID;

/**
 * Событие задачи. Публикуется сервисом как Spring-событие и после коммита транзакции отправляется в Kafka
 * (поле {@code type} различает тип события).
 *
 * @author Timur Khabibullin
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TaskCreatedEvent.class, name = TaskCreatedEvent.TYPE),
        @JsonSubTypes.Type(value = TaskAssignedEvent.class, name = TaskAssignedEvent.TYPE)
})
public sealed interface TaskEvent permits TaskCreatedEvent, TaskAssignedEvent {

    /**
     * Уникальный идентификатор события
     */
    UUID eventId();

    /**
     * Время возникновения события
     */
    Instant occurredAt();

    /**
     * Идентификатор задачи (он же ключ сообщения в Kafka)
     */
    Long taskId();
}
