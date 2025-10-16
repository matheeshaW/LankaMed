package com.lankamed.health.backend.dto;

import com.lankamed.health.backend.model.Appointment;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AppointmentDto {
    private Long appointmentId;
    private LocalDateTime appointmentDateTime;
    private Appointment.Status status;
    private String doctorName;
    private String doctorSpecialization;
    private String hospitalName;
    private String serviceCategoryName;
    private boolean priority;
    private Long doctorId;

    public static AppointmentDto fromAppointment(Appointment appointment) {
        return AppointmentDto.builder()
                .appointmentId(appointment.getAppointmentId())
                .appointmentDateTime(appointment.getAppointmentDateTime())
                .status(appointment.getStatus())
                .doctorName(appointment.getDoctor().getUser().getFirstName() + " " + 
                           appointment.getDoctor().getUser().getLastName())
                .doctorSpecialization(appointment.getDoctor().getSpecialization())
                .doctorId(appointment.getDoctor().getStaffId())
                .hospitalName(appointment.getHospital().getName())
                .serviceCategoryName(appointment.getServiceCategory().getName())
                .priority(appointment.isPriority())
                .build();
    }
}
