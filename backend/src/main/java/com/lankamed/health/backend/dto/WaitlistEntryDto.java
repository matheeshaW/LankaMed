package com.lankamed.health.backend.dto;

import com.lankamed.health.backend.model.WaitlistEntry;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WaitlistEntryDto {
    private Long id;
    private LocalDateTime desiredDateTime;
    private WaitlistEntry.Status status;
    private String doctorName;
    private String doctorSpecialization;
    private String hospitalName;
    private String serviceCategoryName;
    private boolean priority;
    private Long doctorId;
    private String patientName;
    private String patientEmail;

    public static WaitlistEntryDto fromWaitlistEntry(WaitlistEntry entry) {
        return WaitlistEntryDto.builder()
                .id(entry.getId())
                .desiredDateTime(entry.getDesiredDateTime())
                .status(entry.getStatus())
                .doctorName(entry.getDoctor().getUser().getFirstName() + " " + 
                           entry.getDoctor().getUser().getLastName())
                .doctorSpecialization(entry.getDoctor().getSpecialization())
                .doctorId(entry.getDoctor().getStaffId())
                .hospitalName(entry.getHospital().getName())
                .serviceCategoryName(entry.getServiceCategory().getName())
                .priority(entry.isPriority())
                .patientName(entry.getPatient().getUser().getFirstName() + " " + entry.getPatient().getUser().getLastName())
                .patientEmail(entry.getPatient().getUser().getEmail())
                .build();
    }
}
