package com.lankamed.health.backend.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateWaitlistDto {
    @NotNull(message = "Doctor ID is required")
    private Long doctorId;
    
    // Optional: will fallback to first available hospital if null
    private Long hospitalId;
    
    // Optional: will fallback to first available service category if null
    private Long serviceCategoryId;
    
    @NotNull(message = "Desired date and time is required")
    @Future(message = "Desired date must be in the future")
    private LocalDateTime desiredDateTime;
    
    private boolean priority;

    // Optional override for current user resolution (used when running without auth context)
    private String patientEmail;
}
