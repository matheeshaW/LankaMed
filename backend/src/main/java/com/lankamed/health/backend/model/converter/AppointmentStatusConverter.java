package com.lankamed.health.backend.model.converter;

import com.lankamed.health.backend.model.Appointment;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class AppointmentStatusConverter implements AttributeConverter<Appointment.Status, String> {

    @Override
    public String convertToDatabaseColumn(Appointment.Status status) {
        return status == null ? null : status.name().toLowerCase();
    }

    @Override
    public Appointment.Status convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        String upper = dbData.toUpperCase();
        if (upper.equals("SCHEDULED")) return Appointment.Status.PENDING; // map legacy to pending
        if (upper.equals("REJECTED")) return Appointment.Status.CANCELLED; // map legacy to cancelled
        return Appointment.Status.valueOf(upper);
    }
}


