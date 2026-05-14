package com.rh.system.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do Swagger UI / OpenAPI 3.
 * Acesse em: http://localhost:8080/swagger-ui.html
 *
 * Adiciona o campo "Authorize" no Swagger para testar endpoints protegidos por JWT.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HR System API")
                        .description("API REST do Sistema de Gestão de Recursos Humanos")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("HR System")
                                .email("admin@hrsystem.com")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Insira o token JWT obtido no endpoint /api/auth/login")));
    }
}