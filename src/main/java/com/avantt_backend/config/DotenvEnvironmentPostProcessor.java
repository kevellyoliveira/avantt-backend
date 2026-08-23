package com.avantt_backend.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Carrega um arquivo .env na raiz do projeto e injeta como PropertySource com alta precedência.
 * Valores provenientes deste arquivo terão precedência sobre variáveis de ambiente do sistema.
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "dotenvPropertySource";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Path dotenv = Path.of(".env");
        if (!Files.exists(dotenv)) {
            return;
        }

        try (Stream<String> lines = Files.lines(dotenv)) {
            Map<String, Object> map = new HashMap<>();
            lines.map(String::trim)
                    .filter(l -> !l.isEmpty() && !l.startsWith("#"))
                    .forEach(line -> {
                        int idx = line.indexOf('=');
                        if (idx <= 0) return;
                        String key = line.substring(0, idx).trim();
                        String value = line.substring(idx + 1).trim();
                        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
                            value = value.substring(1, value.length() - 1);
                        }
                        map.put(key, value);
                    });

            if (!map.isEmpty()) {
                // addFirst garante precedência sobre outras property sources (incluindo systemEnvironment)
                environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, map));
            }
        } catch (IOException e) {
            System.err.println("Unable to read .env file: " + e.getMessage());
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
