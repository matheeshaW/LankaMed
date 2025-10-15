package com.lankamed.health.backend.dto;

import java.time.LocalDate;

import com.lankamed.health.backend.model.MedicalCondition;

import lombok.Builder;
import lombok.Data;

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
