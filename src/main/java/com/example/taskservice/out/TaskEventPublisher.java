package com.example.taskservice.out;

import com.example.taskservice.dto.event.TaskEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Отправляет события задач в Kafka строго после успешного коммита транзакции, поэтому событие не уйдёт, если изменения в БД откатились.
 * Ключ сообщения — id задачи: события одной задачи попадают в одну партицию и сохраняют порядок.
 *
 * @author Timur Khabibullin
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topic}")
    private final String topic;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(TaskEvent event) {
        String payload;

        try {
            payload = objectMapper.writerFor(TaskEvent.class).writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("Не удалось сериализовать событие {}", event, e);

            return;
        }

        kafkaTemplate.send(topic, String.valueOf(event.taskId()), payload).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Не удалось отправить событие {} в топик {}", event.eventId(), topic, ex);
            } else {
                log.debug("Событие {} отправлено в {}-{}@{}", event.eventId(), result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
            }
        });
    }
}
