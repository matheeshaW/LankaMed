package com.lankamed.health.backend.model.patient;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BloodPressureRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Patient patient;

    private int systolic; // mmHg
    private int diastolic; // mmHg
    private LocalDateTime timestamp;
}
