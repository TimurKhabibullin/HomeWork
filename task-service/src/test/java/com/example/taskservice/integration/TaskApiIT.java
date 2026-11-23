package com.example.taskservice.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционный тест API: PostgreSQL и Kafka поднимаются через Testcontainers
 *
 * @author Timur Khabibullin
 */
@SpringBootTest(properties = "app.kafka.consumer.enabled=false")
@AutoConfigureMockMvc
@Testcontainers
class TaskApiIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static KafkaContainer kafka = new KafkaContainer("apache/kafka:3.8.0");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.kafka.topic}")
    private String topic;

    @DynamicPropertySource
    static void kafkaProps(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Test
    void shouldPassFullTaskLifecycleAndPublishKafkaEvents() throws Exception {
        long userId = createUser("lifecycle-" + UUID.randomUUID() + "@example.com");

        // создание задачи -> 201 и статус NEW
        var created = mvc.perform(post("/api/v1/tasks")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content("{\"title\":\"Write report\",\"description\":\"Q4\"}"))
                         .andExpect(status().isCreated())
                         .andExpect(jsonPath("$.status").value("NEW"))
                         .andExpect(jsonPath("$.assignee").doesNotExist())
                         .andReturn()
                         .getResponse()
                         .getContentAsString();
        long taskId = objectMapper.readTree(created).get("id").asLong();

        // назначение исполнителя
        mvc.perform(put("/api/v1/tasks/{id}/assignee", taskId)
                   .contentType(MediaType.APPLICATION_JSON)
                   .content("{\"userId\":" + userId + "}"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.assignee.id").value(userId));

        // смена статуса: недопустимая и допустимая
        mvc.perform(put("/api/v1/tasks/{id}/status", taskId)
                   .contentType(MediaType.APPLICATION_JSON)
                   .content("{\"status\":\"DONE\"}"))
           .andExpect(status().isConflict());
        mvc.perform(put("/api/v1/tasks/{id}/status", taskId)
                   .contentType(MediaType.APPLICATION_JSON)
                   .content("{\"status\":\"IN_PROGRESS\"}"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // получение по id
        mvc.perform(get("/api/v1/tasks/{id}", taskId))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.title").value("Write report"))
           .andExpect(jsonPath("$.assignee.id").value(userId));

        // события в Kafka: TASK_CREATED и TASK_ASSIGNED для этой задачи
        var events = consumeEventsForTask(taskId, 2);

        assertThat(events).extracting(e -> e.get("type").asText()).containsExactly("TASK_CREATED", "TASK_ASSIGNED");
        assertThat(events.get(1).get("assigneeId").asLong()).isEqualTo(userId);
    }

    @Test
    void shouldPaginateAndIgnoreClientSort() throws Exception {
        for (int i = 0; i < 3; i++) {
            mvc.perform(post("/api/v1/tasks")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("{\"title\":\"Paged " + i + "\"}"))
               .andExpect(status().isCreated());
        }

        mvc.perform(get("/api/v1/tasks").param("size", "2").param("page", "0"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.content.length()").value(2))
           .andExpect(jsonPath("$.size").value(2))
           .andExpect(jsonPath("$.totalElements").isNumber());

        // произвольный sort от клиента игнорируется, а не приводит к 500
        mvc.perform(get("/api/v1/tasks").param("sort", "nonexistent")).andExpect(status().isOk());

        // максимальный размер страницы
        mvc.perform(get("/api/v1/tasks").param("status", "DONE").param("size", "1000"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.size").value(100));
    }

    @Test
    void shouldReturnErrorsOnInvalidRequests() throws Exception {
        mvc.perform(post("/api/v1/tasks").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\" \"}"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.errors.title").exists());

        mvc.perform(get("/api/v1/tasks/{id}", 987654321L)).andExpect(status().isNotFound());

        mvc.perform(post("/api/v1/tasks")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content("{\"title\":\"x\",\"assigneeId\":987654321}"))
           .andExpect(status().isNotFound());

        mvc.perform(put("/api/v1/tasks/{id}/status", 1)
                   .contentType(MediaType.APPLICATION_JSON)
                   .content("{\"status\":\"UNKNOWN\"}"))
           .andExpect(status().isBadRequest());
    }

    private long createUser(String email) throws Exception {
        var body = mvc.perform(post("/api/v1/users")
                              .contentType(MediaType.APPLICATION_JSON)
                              .content("{\"name\":\"Test User\",\"email\":\"" + email + "\"}"))
                      .andExpect(status().isCreated())
                      .andReturn()
                      .getResponse()
                      .getContentAsString();

        return objectMapper.readTree(body).get("id").asLong();
    }

    private List<JsonNode> consumeEventsForTask(long taskId, int expected) throws Exception {
        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "it-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        var result = new ArrayList<JsonNode>();

        try (var consumer = new KafkaConsumer<String, String>(props)) {
            consumer.subscribe(List.of(topic));

            var deadline = System.currentTimeMillis() + 30_000;
            while (result.size() < expected && System.currentTimeMillis() < deadline) {
                for (var record : consumer.poll(Duration.ofMillis(500))) {
                    if (record.key().equals(String.valueOf(taskId))) {
                        result.add(objectMapper.readTree(record.value()));
                    }
                }
            }
        }

        return result;
    }
}
