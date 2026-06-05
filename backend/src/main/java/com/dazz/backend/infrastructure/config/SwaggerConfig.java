package com.dazz.backend.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DAZZ API")
                        .description("""
                                **K-Jazz Insight Navigator** — 뮤지션 아카이빙 플랫폼 API

                                인증이 필요한 엔드포인트는 우측 상단 **Authorize** 버튼에서
                                `Bearer {accessToken}` 형식으로 토큰을 입력하세요.
                                """)
                        .version("v1"))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local"),
                        new Server().url("https://api-stg.dazz.kr").description("Staging")
                ))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("로그인 후 발급된 Access Token")));
    }
}