package com.lankamed.health.backend.dto.patient;

import com.lankamed.health.backend.model.patient.Patient;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class PatientProfileDto {
    private Long patientId;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate dateOfBirth;
    private Patient.Gender gender;
    private String contactNumber;
    private String address;

    public static PatientProfileDto fromPatient(Patient patient) {
        return PatientProfileDto.builder()
                .patientId(patient.getPatientId())
                .firstName(patient.getUser().getFirstName())
                .lastName(patient.getUser().getLastName())
                .email(patient.getUser().getEmail())
                .dateOfBirth(patient.getDateOfBirth())
                .gender(patient.getGender())
                .contactNumber(patient.getContactNumber())
                .address(patient.getAddress())
                .build();
    }
}


