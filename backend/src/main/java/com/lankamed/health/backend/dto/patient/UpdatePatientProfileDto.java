package com.lankamed.health.backend.dto.patient;

import com.lankamed.health.backend.model.patient.Patient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdatePatientProfileDto {
    @NotBlank
    @Size(max = 50)
    private String firstName;

    @NotBlank
    @Size(max = 50)
    private String lastName;

    @Email
    @NotBlank
    @Size(max = 100)
    private String email;

    private LocalDate dateOfBirth;
    private Patient.Gender gender;

    @Size(max = 20)
    private String contactNumber;

    @Size(max = 255)
    private String address;
}


