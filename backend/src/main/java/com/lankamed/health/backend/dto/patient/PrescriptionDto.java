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
        String doctorName = "Unknown Doctor";
        String doctorSpecialization = "Unknown";
        
        try {
            if (prescription.getDoctor() != null) {
                if (prescription.getDoctor().getUser() != null) {
                    String firstName = prescription.getDoctor().getUser().getFirstName() != null ? 
                                      prescription.getDoctor().getUser().getFirstName() : "";
                    String lastName = prescription.getDoctor().getUser().getLastName() != null ? 
                                     prescription.getDoctor().getUser().getLastName() : "";
                    doctorName = (firstName + " " + lastName).trim();
                    if (doctorName.isEmpty()) {
                        doctorName = "Unknown Doctor";
                    }
                }
                doctorSpecialization = prescription.getDoctor().getSpecialization() != null ? 
                                      prescription.getDoctor().getSpecialization() : "Unknown";
            }
        } catch (Exception e) {
            // Keep default values
        }
        
        return PrescriptionDto.builder()
                .prescriptionId(prescription.getPrescriptionId())
                .medicationName(prescription.getMedicationName())
                .dosage(prescription.getDosage())
                .frequency(prescription.getFrequency())
                .startDate(prescription.getStartDate())
                .endDate(prescription.getEndDate())
                .doctorName(doctorName)
                .doctorSpecialization(doctorSpecialization)
                .build();
    }
}
