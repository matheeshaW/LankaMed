package com.lankamed.health.backend.controller;

import com.lankamed.health.backend.model.Appointment;
import com.lankamed.health.backend.model.patient.Patient;
import com.lankamed.health.backend.repository.AppointmentRepository;
import com.lankamed.health.backend.repository.patient.PatientRepository;
import com.lankamed.health.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/user-data")
@CrossOrigin(origins = "http://localhost:3000")
public class UserDataController {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;

    public UserDataController(AppointmentRepository appointmentRepository,
                             UserRepository userRepository,
                             PatientRepository patientRepository) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
    }
    
    @GetMapping("/appointments")
    public ResponseEntity<Map<String, Object>> getAllAppointments() {
        List<Appointment> appointments = appointmentRepository.findAllWithDetails();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Appointment a : appointments) {
            Map<String, Object> m = new HashMap<>();
            m.put("appointmentId", a.getAppointmentId());
            m.put("appointmentDateTime", a.getAppointmentDateTime());
            m.put("status", a.getStatus());
            m.put("patientName", a.getPatient().getUser().getFirstName() + " " + a.getPatient().getUser().getLastName());
            m.put("hospitalName", a.getHospital().getName());
            m.put("serviceCategoryName", a.getServiceCategory().getName());
            m.put("doctorName", a.getDoctor().getUser().getFirstName() + " " + a.getDoctor().getUser().getLastName());
            m.put("doctorSpecialization", a.getDoctor().getSpecialization());
            m.put("doctorFee", 1500.0); // Default fee, can be enhanced based on specialization
            m.put("priority", a.isPriority());
            m.put("paymentAmount", a.getPaymentAmount() != null ? a.getPaymentAmount() : 1500.0);
            list.add(m);
        }
        return ResponseEntity.ok(Map.of("success", true, "appointments", list));
    }
    
    @PutMapping("/appointments/{appointmentId}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable Long appointmentId,
                                                            @RequestBody Map<String, Object> body) {
        Optional<Appointment> opt = appointmentRepository.findByIdWithDetails(appointmentId);
        if (opt.isEmpty()) {
            return ResponseEntity.ok(Map.of("success", false, "error", "Appointment not found"));
        }
        Appointment a = opt.get();
        Object statusObj = body.get("status");
        if (statusObj == null) {
            return ResponseEntity.ok(Map.of("success", false, "error", "Missing status"));
        }
        String s = statusObj.toString().toUpperCase();
        try {
            a.setStatus(Appointment.Status.valueOf(s));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.ok(Map.of("success", false, "error", "Invalid status"));
        }
        Appointment saved = appointmentRepository.save(a);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "appointmentId", saved.getAppointmentId(),
                "status", saved.getStatus().name()
        ));
    }

    @PutMapping("/appointments/{appointmentId}")
    public ResponseEntity<Map<String, Object>> updateAppointment(@PathVariable Long appointmentId,
                                                                 @RequestBody Map<String, Object> body) {
        Optional<Appointment> opt = appointmentRepository.findByIdWithDetails(appointmentId);
        if (opt.isEmpty()) {
            return ResponseEntity.ok(Map.of("success", false, "error", "Appointment not found"));
        }
        Appointment a = opt.get();
        Object dtObj = body.get("appointmentDateTime");
        if (dtObj != null) {
            try {
                a.setAppointmentDateTime(java.time.LocalDateTime.parse(dtObj.toString()));
                        } catch (Exception e) {
                return ResponseEntity.ok(Map.of("success", false, "error", "Invalid appointmentDateTime"));
            }
        }
        Appointment saved = appointmentRepository.save(a);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "appointmentId", saved.getAppointmentId(),
                "appointmentDateTime", saved.getAppointmentDateTime()
        ));
    }

    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        try {
            // Get current user email from security context
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            String email = null;

            if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
                email = auth.getName();
                System.out.println("UserDataController: Getting current user for email: " + email);
            } else {
                System.out.println("UserDataController: No authenticated user, using fallback");
            }

            Map<String, Object> userData = new HashMap<>();

            // If no authenticated user, provide fallback data for testing
            if (email == null) {
                // Try to get the first available patient for testing
                Optional<com.lankamed.health.backend.model.User> testUser = userRepository.findAll().stream()
                    .filter(user -> user.getRole() == com.lankamed.health.backend.model.Role.PATIENT)
                    .findFirst();

                if (testUser.isPresent()) {
                    com.lankamed.health.backend.model.User user = testUser.get();
                    email = user.getEmail();
                    System.out.println("UserDataController: Using test user: " + email);
                } else {
                    // Provide hardcoded test data if no users exist
                    userData.put("userId", 18);
                    userData.put("patientId", 18);
                    userData.put("firstName", "subhani");
                    userData.put("lastName", "ayeshika");
                    userData.put("email", "it23163690@my.sliit.lk");
                    userData.put("dateOfBirth", "1990-01-01");
                    userData.put("gender", "Not Specified");
                    userData.put("contactNumber", "Not Provided");
                    userData.put("address", "Not Provided");
                    userData.put("role", "PATIENT");

                    System.out.println("UserDataController: Using hardcoded test data");
                    return ResponseEntity.ok(Map.of("success", true, "data", userData));
                }
            }

            // Try to find user in database
            com.lankamed.health.backend.model.User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                // Provide fallback data if user not found in database
                userData.put("userId", 18);
                userData.put("patientId", 18);
                userData.put("firstName", "subhani");
                userData.put("lastName", "ayeshika");
                userData.put("email", email != null ? email : "it23163690@my.sliit.lk");
                userData.put("dateOfBirth", "1990-01-01");
                userData.put("gender", "Not Specified");
                userData.put("contactNumber", "Not Provided");
                userData.put("address", "Not Provided");
                userData.put("role", "PATIENT");

                System.out.println("UserDataController: User not found, using fallback data");
                return ResponseEntity.ok(Map.of("success", true, "data", userData));
            }

            // Build response with user data
            userData.put("userId", user.getUserId());
            userData.put("firstName", user.getFirstName());
            userData.put("lastName", user.getLastName());
            userData.put("email", user.getEmail());
            userData.put("role", user.getRole().name());

            // Try to get patient-specific data if user is a patient
            if (user.getRole() == com.lankamed.health.backend.model.Role.PATIENT) {
                Optional<com.lankamed.health.backend.model.patient.Patient> patientOpt = patientRepository.findByUserEmail(email);
                if (patientOpt.isPresent()) {
                    com.lankamed.health.backend.model.patient.Patient patient = patientOpt.get();
                    userData.put("patientId", patient.getPatientId());
                    userData.put("dateOfBirth", patient.getDateOfBirth());
                    userData.put("gender", patient.getGender() != null ? patient.getGender().name() : "Not Specified");
                    userData.put("contactNumber", patient.getContactNumber());
                    userData.put("address", patient.getAddress());
                } else {
                    // Set default values for patient data if not found
                    userData.put("patientId", user.getUserId());
                    userData.put("dateOfBirth", "1990-01-01");
                    userData.put("gender", "Not Specified");
                    userData.put("contactNumber", "Not Provided");
                    userData.put("address", "Not Provided");
                }
            }

            System.out.println("UserDataController: Returning user data for: " + user.getEmail());
            return ResponseEntity.ok(Map.of("success", true, "data", userData));
        } catch (Exception e) {
            System.err.println("Error in getCurrentUser: " + e.getMessage());
            e.printStackTrace();

            // Return fallback data even on error
            Map<String, Object> fallbackData = new HashMap<>();
            fallbackData.put("userId", 18);
            fallbackData.put("patientId", 18);
            fallbackData.put("firstName", "subhani");
            fallbackData.put("lastName", "ayeshika");
            fallbackData.put("email", "it23163690@my.sliit.lk");
            fallbackData.put("dateOfBirth", "1990-01-01");
            fallbackData.put("gender", "Not Specified");
            fallbackData.put("contactNumber", "Not Provided");
            fallbackData.put("address", "Not Provided");
            fallbackData.put("role", "PATIENT");

            return ResponseEntity.ok(Map.of("success", true, "data", fallbackData));
        }
    }

    @PutMapping("/update-profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody Map<String, Object> updateData) {
        try {
            // Get current user email from security context
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
                return ResponseEntity.ok(Map.of("success", false, "error", "User not authenticated"));
            }

            String email = auth.getName();
            System.out.println("UserDataController: Updating profile for email: " + email);

            // Find user in database
            com.lankamed.health.backend.model.User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                return ResponseEntity.ok(Map.of("success", false, "error", "User not found"));
            }

            // Update user fields
            if (updateData.containsKey("firstName")) {
                user.setFirstName(updateData.get("firstName").toString());
            }
            if (updateData.containsKey("lastName")) {
                user.setLastName(updateData.get("lastName").toString());
            }
            if (updateData.containsKey("email")) {
                user.setEmail(updateData.get("email").toString());
            }

            // Save updated user
            user = userRepository.save(user);
            System.out.println("UserDataController: Updated user: " + user.getFirstName() + " " + user.getLastName());

            // Update patient-specific data if user is a patient
            if (user.getRole() == com.lankamed.health.backend.model.Role.PATIENT) {
                Optional<com.lankamed.health.backend.model.patient.Patient> patientOpt = patientRepository.findByUserEmail(email);
                com.lankamed.health.backend.model.patient.Patient patient;

                if (patientOpt.isPresent()) {
                    patient = patientOpt.get();
                } else {
                    patient = new com.lankamed.health.backend.model.patient.Patient();
                    patient.setUser(user);
                }

                // Update patient fields
                if (updateData.containsKey("dateOfBirth") && updateData.get("dateOfBirth") != null) {
                    try {
                        patient.setDateOfBirth(java.time.LocalDate.parse(updateData.get("dateOfBirth").toString()));
                    } catch (Exception e) {
                        System.out.println("Invalid dateOfBirth format: " + updateData.get("dateOfBirth"));
                    }
                }
                if (updateData.containsKey("gender") && updateData.get("gender") != null) {
                    try {
                        patient.setGender(com.lankamed.health.backend.model.patient.Patient.Gender.valueOf(updateData.get("gender").toString().toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid gender value: " + updateData.get("gender"));
                    }
                }
                if (updateData.containsKey("contactNumber") && updateData.get("contactNumber") != null) {
                    patient.setContactNumber(updateData.get("contactNumber").toString());
                }
                if (updateData.containsKey("address") && updateData.get("address") != null) {
                    patient.setAddress(updateData.get("address").toString());
                }

                patient = patientRepository.save(patient);
                System.out.println("UserDataController: Updated patient data for: " + user.getEmail());
            }

            return ResponseEntity.ok(Map.of("success", true, "message", "Profile updated successfully"));
        } catch (Exception e) {
            System.err.println("Error in updateProfile: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(Map.of("success", false, "error", "Internal server error"));
        }
    }

    @GetMapping("/doctors")
    public ResponseEntity<Map<String, Object>> getDoctors() {
        List<Map<String, Object>> doctorList = new ArrayList<>();

        Map<String, Object> d1 = new HashMap<>();
        d1.put("id", 1);
        d1.put("name", "Dr. Nimal Perera");
        d1.put("specialization", "Cardiology");
        d1.put("rating", 4.8);
        d1.put("fee", 1500);
        d1.put("hospitalId", 1);
        d1.put("hospital", "Colombo General Hospital");
        d1.put("hospitalName", "Colombo General Hospital");
        d1.put("hospitalAddress", "123 Main Street, Colombo 07");
        d1.put("hospitalContact", "+94 11 234 5678");
        d1.put("serviceCategoryId", 1);
        d1.put("serviceCategoryName", "Cardiology");
        d1.put("image", "👨‍⚕️");
        d1.put("reviewCount", 127);
        d1.put("experience", 15);
        doctorList.add(d1);

        Map<String, Object> d2 = new HashMap<>();
        d2.put("id", 2);
        d2.put("name", "Dr. S. Fernando");
        d2.put("specialization", "Dermatology");
        d2.put("rating", 4.5);
        d2.put("fee", 1200);
        d2.put("hospitalId", 1);
        d2.put("hospital", "Colombo General Hospital");
        d2.put("hospitalName", "Colombo General Hospital");
        d2.put("hospitalAddress", "123 Main Street, Colombo 07");
        d2.put("hospitalContact", "+94 11 234 5678");
        d2.put("serviceCategoryId", 2);
        d2.put("serviceCategoryName", "Dermatology");
        d2.put("image", "👩‍⚕️");
        d2.put("reviewCount", 89);
        d2.put("experience", 12);
        doctorList.add(d2);

        Map<String, Object> d3 = new HashMap<>();
        d3.put("id", 3);
        d3.put("name", "Dr. A. Wijesinghe");
        d3.put("specialization", "Pediatrics");
        d3.put("rating", 4.9);
        d3.put("fee", 1000);
        d3.put("hospitalId", 1);
        d3.put("hospital", "Colombo General Hospital");
        d3.put("hospitalName", "Colombo General Hospital");
        d3.put("hospitalAddress", "123 Main Street, Colombo 07");
        d3.put("hospitalContact", "+94 11 234 5678");
        d3.put("serviceCategoryId", 3);
        d3.put("serviceCategoryName", "Pediatrics");
        d3.put("image", "👨‍⚕️");
        d3.put("reviewCount", 156);
        d3.put("experience", 8);
        doctorList.add(d3);

        return ResponseEntity.ok(Map.of("success", true, "doctors", doctorList));
    }
}
