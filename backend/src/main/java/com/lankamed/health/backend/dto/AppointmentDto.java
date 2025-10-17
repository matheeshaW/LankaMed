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
    private Double paymentAmount;
    private Double doctorFee;
    private String reason;

    public static AppointmentDto fromAppointment(Appointment appointment) {
        Double doctorConsultationFee = appointment.getDoctor().getConsultationFee();
        Double appointmentPaymentAmount = appointment.getPaymentAmount();

        System.out.println("AppointmentDto Debug for appointment ID: " + appointment.getAppointmentId());
        System.out.println("  Doctor consultation fee: " + doctorConsultationFee);
        System.out.println("  Appointment payment amount: " + appointmentPaymentAmount);
        System.out.println("  Doctor ID: " + appointment.getDoctor().getStaffId());
        System.out.println("  Doctor name: " + appointment.getDoctor().getUser().getFirstName() + " " +
                          appointment.getDoctor().getUser().getLastName());

        // Use doctor's consultation fee, or appointment payment amount, or default
        Double finalPaymentAmount = doctorConsultationFee != null ? doctorConsultationFee :
                                   (appointmentPaymentAmount != null ? appointmentPaymentAmount : 1500.00);

        System.out.println("  Final payment amount to be used: " + finalPaymentAmount);

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
                .paymentAmount(finalPaymentAmount)
                .doctorFee(finalPaymentAmount)
                .reason(appointment.getReason())
                .build();
    }
}
