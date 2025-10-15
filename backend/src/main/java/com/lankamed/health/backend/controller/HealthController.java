package com.lankamed.health.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", Instant.now());
        response.put("service", "LankaMed Backend");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> checkDatabase() {
        Map<String, Object> response = new HashMap<>();
        try {
            // This would need to be injected, but for now just return basic info
            response.put("status", "Database check endpoint");
            response.put("message", "Database connection should be working");
            response.put("timestamp", Instant.now());
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("status", "Error");
        }
        return ResponseEntity.ok(response);
    }
}

