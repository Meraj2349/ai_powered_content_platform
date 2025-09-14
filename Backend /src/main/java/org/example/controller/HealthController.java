package org.example.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.example.service.DatabaseHealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    @Autowired
    private DatabaseHealthService databaseHealthService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();

        boolean dbHealthy = databaseHealthService.isDatabaseHealthy();

        health.put("status", dbHealthy ? "UP" : "DOWN");
        health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        health.put("application", "AI Content Platform");
        health.put("version", "1.0.0");

        // Database health
        Map<String, Object> database = new HashMap<>();
        database.put("status", dbHealthy ? "UP" : "DOWN");
        database.put("type", "MongoDB");

        if (dbHealthy) {
            try {
                Map<String, Object> dbInfo = databaseHealthService.getDatabaseInfo();
                database.putAll(dbInfo);
            } catch (Exception e) {
                database.put("error", e.getMessage());
            }
        }

        health.put("database", database);

        return ResponseEntity.ok(health);
    }

    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> databaseHealth() {
        Map<String, Object> response = databaseHealthService.getDetailedStatus();
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        boolean isHealthy = "healthy".equals(response.get("overall"));
        return isHealthy ? ResponseEntity.ok(response) : ResponseEntity.status(503).body(response);
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> pong = new HashMap<>();
        pong.put("message", "pong");
        pong.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        pong.put("status", "OK");

        return ResponseEntity.ok(pong);
    }
}
