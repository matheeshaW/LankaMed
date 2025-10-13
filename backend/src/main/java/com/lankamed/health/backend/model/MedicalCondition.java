package com.lankamed.health.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "medical_conditions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "condition_id")
    private Long conditionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotBlank
    @Size(max = 255)
    @Column(name = "condition_name", nullable = false)
    private String conditionName;

    @Column(name = "diagnosed_date")
    private LocalDate diagnosedDate;

    @Lob
    private String notes;
}
