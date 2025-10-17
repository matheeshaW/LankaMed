package com.lankamed.health.backend.dto.patient;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WeightRecordDto {
    private double weightKg;
    private LocalDateTime timestamp;
}
