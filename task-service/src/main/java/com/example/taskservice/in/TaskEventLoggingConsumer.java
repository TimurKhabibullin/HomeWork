package com.example.taskservice.in;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Демонстрационный consumer: логирует события, чтобы их можно было увидеть при локальном запуске.
 * Отключается через {@code app.kafka.consumer.enabled=false}.
 *
 * @author Timur Khabibullin
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.kafka.consumer.enabled", havingValue = "true", matchIfMissing = true)
public class TaskEventLoggingConsumer {

    @KafkaListener(topics = "${app.kafka.topic}")
    public void onEvent(String payload) {
        log.info("Получено событие задачи: {}", payload);
    }
}
