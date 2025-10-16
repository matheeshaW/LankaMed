package com.lankamed.health.backend.service;

import com.lankamed.health.backend.model.Role;
import com.lankamed.health.backend.model.User;
import com.lankamed.health.backend.model.patient.Patient;
import com.lankamed.health.backend.repository.UserRepository;
import com.lankamed.health.backend.repository.patient.PatientRepository;
import com.lankamed.health.backend.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PatientRepository patientRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public User register(String firstName, String lastName, String email, String rawPassword, Role role) {
        System.out.println("AuthService: Starting registration for email: " + email + ", role: " + role);
        
        if (userRepository.existsByEmail(email)) {
            System.out.println("AuthService: Email already exists: " + email);
            throw new IllegalArgumentException("Email already registered");
        }
        
        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .role(role)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .build();
        User savedUser = userRepository.save(user);
        System.out.println("AuthService: User saved with ID: " + savedUser.getUserId());
        
        // Create Patient record for PATIENT role users
        if (role == Role.PATIENT) {
            System.out.println("AuthService: Creating Patient record for user ID: " + savedUser.getUserId());
            Patient patient = Patient.builder()
                    .user(savedUser)
                    .build();
            Patient savedPatient = patientRepository.save(patient);
            System.out.println("AuthService: Patient record created with ID: " + savedPatient.getPatientId());
        }
        
        return savedUser;
    }

    public String login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        String role = authentication.getAuthorities().stream().findFirst().orElseThrow().getAuthority().replace("ROLE_", "");
        return jwtUtil.generateToken(email, role);
    }
}
