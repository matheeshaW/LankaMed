package com.lankamed.health.backend.dto.patient;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BloodPressureRecordDto {
    private int systolic;
    private int diastolic;
    private LocalDateTime timestamp;
}
