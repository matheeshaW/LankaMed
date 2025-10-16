package com.lankamed.health.backend.dto.patient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateMedicalConditionDto {
    @NotBlank
    @Size(max = 255)
    private String conditionName;

    private LocalDate diagnosedDate;

    @Size(max = 1000)
    private String notes;
}


