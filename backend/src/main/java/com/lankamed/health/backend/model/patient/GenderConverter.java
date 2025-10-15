package com.lankamed.health.backend.model.patient;
import com.lankamed.health.backend.model.patient.Patient;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class GenderConverter implements AttributeConverter<Patient.Gender, String> {

    @Override
    public String convertToDatabaseColumn(Patient.Gender attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public Patient.Gender convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return Patient.Gender.valueOf(dbData.toUpperCase());
        } catch (IllegalArgumentException ex) {
            // fallback: try exact match
            for (Patient.Gender g : Patient.Gender.values()) {
                if (g.name().equals(dbData)) return g;
            }
            return null;
        }
    }
}
