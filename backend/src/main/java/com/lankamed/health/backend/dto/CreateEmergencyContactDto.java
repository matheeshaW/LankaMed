package com.lankamed.health.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateEmergencyContactDto {
    @NotBlank
    @Size(max = 100)
    private String fullName;

    @NotBlank
    @Size(max = 50)
    private String relationship;

    @NotBlank
    @Size(max = 20)
    private String phone;

    @Size(max = 120)
    private String email;

    @Size(max = 255)
    private String address;
}


