package com.example.taskservice.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * @author Timur Khabibullin
 */
@Configuration
public class KafkaTopicConfig {

    @Bean
    NewTopic taskEventsTopic(@Value("${app.kafka.topic}") String topic, @Value("${app.kafka.partitions}") int partitions) {
        return TopicBuilder.name(topic).partitions(partitions).replicas(1).build();
    }
}
