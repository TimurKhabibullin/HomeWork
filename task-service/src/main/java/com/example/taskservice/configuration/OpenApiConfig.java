package com.example.taskservice.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Timur Khabibullin
 */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI taskServiceOpenApi() {
        return new OpenAPI().info(new Info().title("Task Service API").version("v1"));
    }
}
