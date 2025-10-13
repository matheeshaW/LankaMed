package com.lankamed.health.backend.dto;

import com.lankamed.health.backend.model.MedicalCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class MedicalConditionDto {
    private Long conditionId;
    private String conditionName;
    private LocalDate diagnosedDate;
    private String notes;

    public static MedicalConditionDto fromMedicalCondition(MedicalCondition condition) {
        return MedicalConditionDto.builder()
                .conditionId(condition.getConditionId())
                .conditionName(condition.getConditionName())
                .diagnosedDate(condition.getDiagnosedDate())
                .notes(condition.getNotes())
                .build();
    }
}
