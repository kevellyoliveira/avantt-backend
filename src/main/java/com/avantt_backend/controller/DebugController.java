package com.avantt_backend.controller;

import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/debug")
public class DebugController {

    private final DataSource dataSource;
    private final Environment env;

    public DebugController(DataSource dataSource, Environment env) {
        this.dataSource = dataSource;
        this.env = env;
    }

    @GetMapping("/db")
    public ResponseEntity<?> dbInfo() {
        Map<String, Object> m = new HashMap<>();
        String configuredUrl = env.getProperty("spring.datasource.url");
        m.put("configuredUrl", configuredUrl == null ? "<not set>" : configuredUrl);
        try (Connection c = dataSource.getConnection()) {
            m.put("jdbcUrl", c.getMetaData().getURL());
            m.put("databaseProduct", c.getMetaData().getDatabaseProductName());
            m.put("databaseProductVersion", c.getMetaData().getDatabaseProductVersion());
            // catalog is the current database/schema
            m.put("currentCatalog", c.getCatalog());
            m.put("user", c.getMetaData().getUserName());
        } catch (Exception ex) {
            m.put("error", ex.toString());
        }
        return ResponseEntity.ok(m);
    }
}
