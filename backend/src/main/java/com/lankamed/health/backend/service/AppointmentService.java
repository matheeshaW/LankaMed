package com.lankamed.health.backend.service;

import com.lankamed.health.backend.dto.AppointmentDto;
import com.lankamed.health.backend.model.patient.Patient;
import com.lankamed.health.backend.repository.AppointmentRepository;
import com.lankamed.health.backend.repository.patient.PatientRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;

    public AppointmentService(AppointmentRepository appointmentRepository, PatientRepository patientRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
    }

    public List<AppointmentDto> getPatientAppointments() {
        String email = getCurrentUserEmail();
        return appointmentRepository.findByPatientUserEmailOrderByAppointmentDateTimeDesc(email)
                .stream()
                .map(AppointmentDto::fromAppointment)
                .collect(Collectors.toList());
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
