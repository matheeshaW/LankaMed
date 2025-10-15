package com.lankamed.health.backend.model.patient;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class AllergySeverityConverter implements AttributeConverter<Allergy.Severity, String> {
    @Override
    public String convertToDatabaseColumn(Allergy.Severity severity) {
        return severity == null ? null : severity.name();
    }

    @Override
    public Allergy.Severity convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        for (Allergy.Severity s : Allergy.Severity.values()) {
            if (s.name().equalsIgnoreCase(dbData)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown severity: " + dbData);
    }
}


