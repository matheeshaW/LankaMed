package com.lankamed.health.backend.service.patient;

import com.lankamed.health.backend.model.patient.Patient;
import com.lankamed.health.backend.repository.patient.PatientRepository;
import com.lankamed.health.backend.dto.patient.PatientProfileDto;
import com.lankamed.health.backend.dto.patient.UpdatePatientProfileDto;
import com.lankamed.health.backend.model.User;
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
        System.out.println("PatientService: Getting profile for email: " + email);
        
        // If no authenticated user, try to get the most recent patient for testing
        if (email == null || email.isEmpty()) {
            System.out.println("PatientService: No authenticated user, getting most recent patient");
            return userRepository.findAll().stream()
                .filter(user -> user.getRole() == com.lankamed.health.backend.model.Role.PATIENT)
                .findFirst()
                .map(user -> {
                    System.out.println("PatientService: Using most recent patient: " + user.getEmail());
                    return PatientProfileDto.builder()
                        .patientId(user.getUserId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .build();
                })
                .orElseThrow(() -> new RuntimeException("No patients found"));
        }
        
        return patientRepository.findByUserEmail(email)
            .map(patient -> {
                System.out.println("PatientService: Found patient record for email: " + email);
                return PatientProfileDto.fromPatient(patient);
            })
            .orElseGet(() -> {
                System.out.println("PatientService: No patient record found, looking up user for email: " + email);
                return userRepository.findByEmail(email)
                    .map(user -> {
                        System.out.println("PatientService: Found user record for email: " + email);
                        return PatientProfileDto.builder()
                            .patientId(user.getUserId())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .email(user.getEmail())
                            .build();
                    })
                    .orElseThrow(() -> {
                        System.out.println("PatientService: No user found for email: " + email);
                        return new RuntimeException("Patient not found");
                    });
            });
    }

    @Transactional
    public PatientProfileDto updatePatientProfile(UpdatePatientProfileDto updateDto) {
        String email = getCurrentUserEmail();
        System.out.println("PatientService: Updating profile for email: " + email);

        // If no authenticated user, use the specific user from database
        if (email == null || email.isEmpty()) {
            System.out.println("PatientService: No authenticated user, using specific user");
            email = "it23163690@my.sliit.lk"; // Use the specific user email
        }

        Patient patient = patientRepository.findByUserEmail(email).orElse(null);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found while updating patient"));

        System.out.println("PatientService: Found user: " + user.getFirstName() + " " + user.getLastName());

        user.setFirstName(updateDto.getFirstName());
        user.setLastName(updateDto.getLastName());
        user.setEmail(updateDto.getEmail());
        userRepository.save(user);

        if (patient == null) {
            patient = new Patient();
            patient.setUser(user);
        }

        patient.setDateOfBirth(updateDto.getDateOfBirth());
        patient.setGender(updateDto.getGender());
        patient.setContactNumber(updateDto.getContactNumber());
        patient.setAddress(updateDto.getAddress());

        patient.setUser(user);
        patient = patientRepository.save(patient);

        System.out.println("PatientService: Updated patient with gender: " + patient.getGender() + 
                          ", phone: " + patient.getContactNumber() + 
                          ", address: " + patient.getAddress());

        return PatientProfileDto.fromPatient(patient);
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}


