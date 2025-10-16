package com.lankamed.health.backend.controller.patient;

import com.lankamed.health.backend.dto.patient.HealthMetricDto;
import com.lankamed.health.backend.service.patient.HealthMetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients/me/health-metrics")
@CrossOrigin(origins = "http://localhost:3000")
public class HealthMetricController {
    @Autowired
    private HealthMetricService healthMetricService;

    @GetMapping
    public ResponseEntity<List<HealthMetricDto>> getAllMetrics() {
        return ResponseEntity.ok(healthMetricService.getMetricsForCurrentPatient());
    }

    @GetMapping("/latest")
    public ResponseEntity<HealthMetricDto> getLatestMetric() {
        return healthMetricService.getLatestMetric()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping
    public ResponseEntity<HealthMetricDto> addMetric(@RequestBody HealthMetricDto dto) {
        return ResponseEntity.ok(healthMetricService.addMetric(dto));
    }
}
