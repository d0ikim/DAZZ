package com.dazz.backend.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DAZZ API")
                        .description("K-Jazz Insight Navigator — 뮤지션 아카이빙 플랫폼 API")
                        .version("v1"));
    }
}
