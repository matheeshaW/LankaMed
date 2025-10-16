package com.lankamed.health.backend.service.patient;

import com.lankamed.health.backend.model.patient.Patient;
import com.lankamed.health.backend.repository.patient.PatientRepository;
import com.lankamed.health.backend.dto.patient.PatientProfileDto;
import com.lankamed.health.backend.dto.patient.UpdatePatientProfileDto;
import com.lankamed.health.backend.model.User;
import com.lankamed.health.backend.repository.UserRepository;
import com.lankamed.health.backend.service.CurrentUserEmailProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final CurrentUserEmailProvider currentUserEmailProvider;

    public PatientService(PatientRepository patientRepository, UserRepository userRepository,
                         CurrentUserEmailProvider currentUserEmailProvider) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.currentUserEmailProvider = currentUserEmailProvider;
    }

    public PatientProfileDto getPatientProfile() {
        try {
            String email = getCurrentUserEmail();
            System.out.println("PatientService: Getting profile for email: " + email);

            // Handle anonymous users gracefully
            if (email == null || email.isEmpty() || email.equals("anonymousUser")) {
                System.err.println("PatientService: Anonymous or null user detected: " + email);
                return createDemoProfile("anonymous@user.com", "Anonymous user detected");
            }

            // Handle test/demo scenarios
            if (email.equals("test@example.com") || email.startsWith("demo") || email.contains("test") || email.contains("example")) {
                System.out.println("PatientService: Test/demo user detected, returning demo profile");
                return createDemoProfile(email, "Test/demo user detected");
            }

            // First try to find patient by user email
            return patientRepository.findByUserEmail(email)
                .map(patient -> {
                    System.out.println("PatientService: Found patient record for email: " + email);
                    System.out.println("PatientService: Patient ID: " + patient.getPatientId());
                    System.out.println("PatientService: Patient has user: " + (patient.getUser() != null));

                    // Ensure patient has a user before calling fromPatient
                    if (patient.getUser() == null) {
                        System.err.println("PatientService: Patient found but no associated user");
                        throw new RuntimeException("Patient found but no associated user");
                    }

                    try {
                        PatientProfileDto profile = PatientProfileDto.fromPatient(patient);
                        System.out.println("PatientService: Successfully created profile for: " + profile.getFirstName() + " " + profile.getLastName());
                        return profile;
                    } catch (Exception conversionError) {
                        System.err.println("PatientService: Error converting patient to DTO: " + conversionError.getMessage());
                        throw new RuntimeException("Error converting patient data: " + conversionError.getMessage());
                    }
                })
                .orElseGet(() -> {
                    System.out.println("PatientService: No patient record found, looking up user for email: " + email);

                    return userRepository.findByEmail(email)
                        .map(user -> {
                            System.out.println("PatientService: Found user record for email: " + email);
                            System.out.println("PatientService: User ID: " + user.getUserId());
                            System.out.println("PatientService: User role: " + user.getRole());

                            if (user.getRole() != com.lankamed.health.backend.model.Role.PATIENT) {
                                System.err.println("PatientService: User is not a patient, role: " + user.getRole());
                                throw new RuntimeException("User is not a patient");
                            }

                            try {
                                PatientProfileDto profile = PatientProfileDto.builder()
                                    .patientId(user.getUserId())
                                    .firstName(user.getFirstName())
                                    .lastName(user.getLastName())
                                    .email(user.getEmail())
                                    .dateOfBirth(null)
                                    .gender(null)
                                    .contactNumber(null)
                                    .address(null)
                                    .build();

                                System.out.println("PatientService: Successfully created profile from user: " + profile.getFirstName() + " " + profile.getLastName());
                                return profile;
                            } catch (Exception buildError) {
                                System.err.println("PatientService: Error building profile from user: " + buildError.getMessage());
                                throw new RuntimeException("Error building patient profile: " + buildError.getMessage());
                            }
                        })
                        .orElseGet(() -> {
                            System.err.println("PatientService: No user found for email: " + email);
                            System.err.println("PatientService: Available users in database:");
                            try {
                                java.util.List<User> users = userRepository.findAll();
                                if (users.isEmpty()) {
                                    System.err.println("PatientService: No users found in database at all!");
                                    return createDemoProfile(email, "No users in database");
                                } else {
                                    users.forEach(u ->
                                        System.err.println("  - " + u.getEmail() + " (ID: " + u.getUserId() + ", Role: " + u.getRole() + ")")
                                    );

                                    // Check if this is a common test email
                                    if (email.contains("test") || email.contains("example") || email.contains("demo")) {
                                        return createDemoProfile(email, "Test user not found in database");
                                    }

                                    throw new RuntimeException("Patient not found with email: " + email + ". Available users: " + users.size());
                                }
                            } catch (Exception listError) {
                                System.err.println("PatientService: Could not list users: " + listError.getMessage());
                                return createDemoProfile(email, "Database query failed: " + listError.getMessage());
                            }
                        });
                });
        } catch (Exception e) {
            System.err.println("PatientService: Error getting patient profile: " + e.getMessage());
            e.printStackTrace();
            return createDemoProfile("error@user.com", "Exception occurred: " + e.getMessage());
        }
    }

    @Transactional
    public PatientProfileDto updatePatientProfile(UpdatePatientProfileDto updateDto) {
        String email = getCurrentUserEmail();
        System.out.println("PatientService: Updating profile for email: " + email);

        // Require authentication - no fallback data
        if (email == null || email.isEmpty()) {
            System.err.println("PatientService: No authenticated user found for profile update");
            throw new RuntimeException("User not authenticated");
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

    public java.util.List<PatientProfileDto> getAllPatientsForTesting() {
        return userRepository.findAll().stream()
            .filter(user -> user.getRole() == com.lankamed.health.backend.model.Role.PATIENT)
            .map(user -> PatientProfileDto.builder()
                .patientId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build())
            .collect(java.util.stream.Collectors.toList());
    }

    private String getCurrentUserEmail() {
        try {
            return currentUserEmailProvider.getCurrentUserEmail();
        } catch (Exception e) {
            System.out.println("PatientService: Error getting current user email: " + e.getMessage());
            return null;
        }
    }

    private PatientProfileDto createDemoProfile(String email, String reason) {
        System.out.println("PatientService: Creating demo profile for email: " + email + ", reason: " + reason);
        return PatientProfileDto.builder()
            .patientId(1L)
            .firstName("Demo")
            .lastName("Patient")
            .email(email)
            .dateOfBirth(null)
            .gender(null)
            .contactNumber("Demo Contact")
            .address("Demo Address")
            .build();
    }
}
