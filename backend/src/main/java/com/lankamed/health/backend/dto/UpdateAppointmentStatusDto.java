package com.lankamed.health.backend.dto;

import com.lankamed.health.backend.model.Appointment;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAppointmentStatusDto {
    @NotNull(message = "Status is required")
    private Appointment.Status status;
}
