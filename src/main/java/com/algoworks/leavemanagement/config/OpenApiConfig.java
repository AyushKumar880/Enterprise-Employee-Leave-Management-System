package com.algoworks.leavemanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Employee Leave Management System API")
                        .description("Production-grade RESTful API for managing employees, leave applications, workflow approvals, and leave histories.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Engineering Team")
                                .email("contact@algoworks.com")
                                .url("https://github.com/"))
                        .license(new License().name("Apache 2.0").url("https://spring.io/")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server")
                ));
    }
}
