package com.lankamed.health.backend.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateAppointmentDto {
    @NotNull(message = "Doctor ID is required")
    private Long doctorId;
    
    @NotNull(message = "Hospital ID is required")
    private Long hospitalId;
    
    @NotNull(message = "Service category ID is required")
    private Long serviceCategoryId;
    
    @NotNull(message = "Appointment date and time is required")
    @Future(message = "Appointment date must be in the future")
    private LocalDateTime appointmentDateTime;
    
    private String reason;

    private boolean priority;
}
