package com.example.authservice.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Timur Khabibullin
 */
@Configuration
public class OpenApiConfig {

    public static final String BEARER_SCHEME_NAME = "bearerAuth";

    @Bean
    OpenAPI authServiceOpenApi() {
        var bearer = new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT");

        return new OpenAPI()
                .info(new Info().title("Auth Service API").version("v1"))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME_NAME, bearer));
    }
}
