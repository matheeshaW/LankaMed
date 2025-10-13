package com.lankamed.health.backend.service;

import com.lankamed.health.backend.dto.PatientProfileDto;
import com.lankamed.health.backend.dto.UpdatePatientProfileDto;
import com.lankamed.health.backend.model.Patient;
import com.lankamed.health.backend.model.User;
import com.lankamed.health.backend.repository.PatientRepository;
import com.lankamed.health.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    public PatientService(PatientRepository patientRepository, UserRepository userRepository) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
    }

    public PatientProfileDto getPatientProfile() {
        String email = getCurrentUserEmail();
    return patientRepository.findByUserEmail(email)
        .map(PatientProfileDto::fromPatient)
        .orElseGet(() -> {
            // If patient row doesn't exist yet, try to build a DTO from the User record
            return userRepository.findByEmail(email)
                .map(user -> PatientProfileDto.builder()
                    .patientId(user.getUserId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .build())
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        });
    }

    @Transactional
    public PatientProfileDto updatePatientProfile(UpdatePatientProfileDto updateDto) {
        String email = getCurrentUserEmail();

        // Load existing patient if present
        Patient patient = patientRepository.findByUserEmail(email).orElse(null);

        // Load user (should exist)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found while updating patient"));

        // Update user fields first (managed entity)
        user.setFirstName(updateDto.getFirstName());
        user.setLastName(updateDto.getLastName());
        user.setEmail(updateDto.getEmail());
        userRepository.save(user);

        if (patient == null) {
            // Create a new Patient and link to managed user. Do NOT set patientId explicitly when using @MapsId.
            patient = new Patient();
            patient.setUser(user);
        }

        // Update patient-specific fields
        patient.setDateOfBirth(updateDto.getDateOfBirth());
        patient.setGender(updateDto.getGender());
        patient.setContactNumber(updateDto.getContactNumber());
        patient.setAddress(updateDto.getAddress());

        // Ensure relationship is up-to-date and save patient
        patient.setUser(user);
        patient = patientRepository.save(patient);

        return PatientProfileDto.fromPatient(patient);
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
