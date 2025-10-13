package com.lankamed.health.backend.dto;

import com.lankamed.health.backend.model.Allergy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAllergyDto {
    @NotBlank
    @Size(max = 255)
    private String allergyName;

    @NotNull
    private Allergy.Severity severity;

    @Size(max = 1000)
    private String notes;
}
