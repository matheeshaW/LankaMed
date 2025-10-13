package com.lankamed.health.backend.dto;

import com.lankamed.health.backend.model.Allergy;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AllergyDto {
    private Long allergyId;
    private String allergyName;
    private Allergy.Severity severity;
    private String notes;

    public static AllergyDto fromAllergy(Allergy allergy) {
        return AllergyDto.builder()
                .allergyId(allergy.getAllergyId())
                .allergyName(allergy.getAllergyName())
                .severity(allergy.getSeverity())
                .notes(allergy.getNotes())
                .build();
    }
}
