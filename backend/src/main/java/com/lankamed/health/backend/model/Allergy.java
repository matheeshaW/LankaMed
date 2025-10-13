package com.lankamed.health.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "allergies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Allergy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "allergy_id")
    private Long allergyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotBlank
    @Size(max = 255)
    @Column(name = "allergy_name", nullable = false)
    private String allergyName;

    @Convert(converter = AllergySeverityConverter.class)
    @Column(nullable = false)
    private Severity severity;

    @Lob
    private String notes;

    public enum Severity {
        MILD, MODERATE, SEVERE
    }
}
