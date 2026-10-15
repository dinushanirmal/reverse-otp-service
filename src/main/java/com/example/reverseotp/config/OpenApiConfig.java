package com.example.reverseotp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Reverse OTP Service API")
                        .version("1.0.0")
                        .description("Spring Boot REST API for Reverse OTP (MO SMS) verification. Designed for Google Cloud Run deployment.")
                        .contact(new Contact().name("Engineering Team"))
                        .license(new License().name("Apache 2.0")));
    }
}
