package com.lankamed.health.backend.dto.patient;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HealthMetricDto {
    private int systolic;
    private int diastolic;
    private int heartRate;
    private int spo2;
    private LocalDateTime timestamp;
}
