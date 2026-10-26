package com.avantt_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Arquivo de configuração da aplicação. Use para beans e configurações gerais.
 * Aqui configuramos CORS para permitir conexões do frontend.
 */
@Configuration
@EnableWebMvc
public class AppConfig implements WebMvcConfigurer {

    // Defina em application.properties: app.cors.allowed-origins=http://localhost:8443,https://meu-frontend.com
    @Value("${app.cors.allowed-origins:http://localhost:8443}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = allowedOrigins.split(",");
        registry.addMapping("/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
