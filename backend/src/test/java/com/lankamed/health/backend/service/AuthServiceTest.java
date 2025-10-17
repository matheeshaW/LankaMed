package com.lankamed.health.backend.service;

import com.lankamed.health.backend.model.Role;
import com.lankamed.health.backend.model.User;
import com.lankamed.health.backend.model.patient.Patient;
import com.lankamed.health.backend.repository.UserRepository;
import com.lankamed.health.backend.repository.patient.PatientRepository;
import com.lankamed.health.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        // Setup common test data
    }

    @Test
    void register_Success() {
        // Given
        String firstName = "John";
        String lastName = "Doe";
        String email = "john.doe@example.com";
        String password = "password123";
        Role role = Role.PATIENT;

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUserId(1L);
            return user;
        });
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> {
            Patient patient = invocation.getArgument(0);
            patient.setPatientId(1L);
            return patient;
        });

        // When
        User result = authService.register(firstName, lastName, email, password, role);

        // Then
        assertNotNull(result);
        assertEquals(firstName, result.getFirstName());
        assertEquals(lastName, result.getLastName());
        assertEquals(email, result.getEmail());
        assertEquals("hashedPassword", result.getPasswordHash());
        assertEquals(role, result.getRole());

        verify(userRepository).existsByEmail(email);
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void register_EmailAlreadyExists() {
        // Given
        String firstName = "John";
        String lastName = "Doe";
        String email = "john.doe@example.com";
        String password = "password123";
        Role role = Role.PATIENT;

        when(userRepository.existsByEmail(email)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.register(firstName, lastName, email, password, role);
        });

        assertEquals("Email already registered", exception.getMessage());
        verify(userRepository).existsByEmail(email);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void login_Success() {
        // Given
        String email = "john.doe@example.com";
        String password = "password123";
        String expectedToken = "jwt-token";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getAuthorities())
                .thenReturn((java.util.Collection) List.of(new SimpleGrantedAuthority("ROLE_PATIENT")));
        when(jwtUtil.generateToken(email, "PATIENT")).thenReturn(expectedToken);

        // When
        String result = authService.login(email, password);

        // Then
        assertEquals(expectedToken, result);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(email, "PATIENT");
    }

    @Test
    void login_WithDoctorRole() {
        // Given
        String email = "doctor@example.com";
        String password = "password123";
        String expectedToken = "jwt-token";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getAuthorities())
                .thenReturn((java.util.Collection) List.of(new SimpleGrantedAuthority("ROLE_DOCTOR")));
        when(jwtUtil.generateToken(email, "DOCTOR")).thenReturn(expectedToken);

        // When
        String result = authService.login(email, password);

        // Then
        assertEquals(expectedToken, result);
        verify(jwtUtil).generateToken(email, "DOCTOR");
    }

    @Test
    void login_WithAdminRole() {
        // Given
        String email = "admin@example.com";
        String password = "password123";
        String expectedToken = "jwt-token";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getAuthorities())
                .thenReturn((java.util.Collection) List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(jwtUtil.generateToken(email, "ADMIN")).thenReturn(expectedToken);

        // When
        String result = authService.login(email, password);

        // Then
        assertEquals(expectedToken, result);
        verify(jwtUtil).generateToken(email, "ADMIN");
    }
}
