package com.lankamed.health.backend.service;

import com.lankamed.health.backend.dto.AppointmentDto;
import com.lankamed.health.backend.dto.CreateAppointmentDto;
import com.lankamed.health.backend.dto.UpdateAppointmentStatusDto;
import com.lankamed.health.backend.model.Appointment;
import com.lankamed.health.backend.model.Hospital;
import com.lankamed.health.backend.model.Patient;
import com.lankamed.health.backend.model.ServiceCategory;
import com.lankamed.health.backend.model.StaffDetails;
import com.lankamed.health.backend.repository.AppointmentRepository;
import com.lankamed.health.backend.repository.HospitalRepository;
import com.lankamed.health.backend.repository.PatientRepository;
import com.lankamed.health.backend.repository.ServiceCategoryRepository;
import com.lankamed.health.backend.repository.StaffDetailsRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final HospitalRepository hospitalRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final StaffDetailsRepository staffDetailsRepository;

    public AppointmentService(AppointmentRepository appointmentRepository, 
                            PatientRepository patientRepository,
                            HospitalRepository hospitalRepository,
                            ServiceCategoryRepository serviceCategoryRepository,
                            StaffDetailsRepository staffDetailsRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.hospitalRepository = hospitalRepository;
        this.serviceCategoryRepository = serviceCategoryRepository;
        this.staffDetailsRepository = staffDetailsRepository;
    }

    public List<AppointmentDto> getPatientAppointments() {
        String email = getCurrentUserEmail();
        return appointmentRepository.findByPatientUserEmailOrderByAppointmentDateTimeDesc(email)
                .stream()
                .map(AppointmentDto::fromAppointment)
                .collect(Collectors.toList());
    }

    public AppointmentDto createAppointment(CreateAppointmentDto createAppointmentDto) {
        String email = getCurrentUserEmail();
        Patient patient = patientRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        StaffDetails doctor = staffDetailsRepository.findById(createAppointmentDto.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        Hospital hospital = hospitalRepository.findById(createAppointmentDto.getHospitalId())
                .orElseThrow(() -> new RuntimeException("Hospital not found"));

        ServiceCategory serviceCategory = serviceCategoryRepository.findById(createAppointmentDto.getServiceCategoryId())
                .orElseThrow(() -> new RuntimeException("Service category not found"));

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .hospital(hospital)
                .serviceCategory(serviceCategory)
                .appointmentDateTime(createAppointmentDto.getAppointmentDateTime())
                .status(Appointment.Status.PENDING)
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return AppointmentDto.fromAppointment(savedAppointment);
    }

    public List<AppointmentDto> getAllAppointments() {
        return appointmentRepository.findAll()
                .stream()
                .map(AppointmentDto::fromAppointment)
                .collect(Collectors.toList());
    }

    public AppointmentDto updateAppointmentStatus(Long appointmentId, UpdateAppointmentStatusDto updateDto) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        appointment.setStatus(updateDto.getStatus());
        Appointment savedAppointment = appointmentRepository.save(appointment);
        return AppointmentDto.fromAppointment(savedAppointment);
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
