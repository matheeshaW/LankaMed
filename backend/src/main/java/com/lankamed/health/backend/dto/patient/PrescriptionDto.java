package com.lankamed.health.backend.dto.patient;

import com.lankamed.health.backend.model.patient.Prescription;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class PrescriptionDto {
    private Long prescriptionId;
    private String medicationName;
    private String dosage;
    private String frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private String doctorName;
    private String doctorSpecialization;

    public static PrescriptionDto fromPrescription(Prescription prescription) {
        return PrescriptionDto.builder()
                .prescriptionId(prescription.getPrescriptionId())
                .medicationName(prescription.getMedicationName())
                .dosage(prescription.getDosage())
                .frequency(prescription.getFrequency())
                .startDate(prescription.getStartDate())
                .endDate(prescription.getEndDate())
                .doctorName(prescription.getDoctor().getUser().getFirstName() + " " + 
                           prescription.getDoctor().getUser().getLastName())
                .doctorSpecialization(prescription.getDoctor().getSpecialization())
                .build();
    }
}
