package com.lankamed.health.backend.service.patient;

import com.lankamed.health.backend.dto.patient.PatientProfileDto;
import com.lankamed.health.backend.dto.patient.UpdatePatientProfileDto;
import com.lankamed.health.backend.model.patient.Patient;
import com.lankamed.health.backend.repository.patient.PatientRepository;
import com.lankamed.health.backend.repository.UserRepository;

import com.lankamed.health.backend.model.User;
import com.lankamed.health.backend.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private PatientService patientService;

    private User testUser;
    private Patient testPatient;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .passwordHash("hashedPassword")
                .role(Role.PATIENT)
                .createdAt(Instant.now())
                .build();

        testPatient = Patient.builder()
                .patientId(1L)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender(Patient.Gender.MALE)
                .contactNumber("+1234567890")
                .address("123 Main St, City")
                .user(testUser)
                .build();

        // Mock SecurityContext
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("john.doe@example.com");
    }

    @Test
    void getPatientProfile_Success() {
        // Given
        when(patientRepository.findByUserEmail("john.doe@example.com"))
                .thenReturn(Optional.of(testPatient));

        // When
        PatientProfileDto result = patientService.getPatientProfile();

        // Then
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals(LocalDate.of(1990, 1, 1), result.getDateOfBirth());
        assertEquals(Patient.Gender.MALE, result.getGender());
        assertEquals("+1234567890", result.getContactNumber());
        assertEquals("123 Main St, City", result.getAddress());

        verify(patientRepository).findByUserEmail("john.doe@example.com");
    }

    @Test
    void getPatientProfile_PatientNotFound() {
        // Given
        when(patientRepository.findByUserEmail("john.doe@example.com"))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            patientService.getPatientProfile();
        });

        assertEquals("Patient not found", exception.getMessage());
        verify(patientRepository).findByUserEmail("john.doe@example.com");
    }

    @Test
    void updatePatientProfile_Success() {
        // Given
        UpdatePatientProfileDto updateDto = new UpdatePatientProfileDto();
        updateDto.setFirstName("Jane");
        updateDto.setLastName("Smith");
        updateDto.setEmail("jane.smith@example.com");
        updateDto.setDateOfBirth(LocalDate.of(1985, 5, 15));
        updateDto.setGender(Patient.Gender.FEMALE);
        updateDto.setContactNumber("+9876543210");
        updateDto.setAddress("456 Oak Ave, Town");

        when(patientRepository.findByUserEmail("john.doe@example.com"))
                .thenReturn(Optional.of(testPatient));
    when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        // When
        PatientProfileDto result = patientService.updatePatientProfile(updateDto);

        // Then
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void updatePatientProfile_PatientNotFound() {
        // Given
        UpdatePatientProfileDto updateDto = new UpdatePatientProfileDto();
        updateDto.setFirstName("Jane");

        // If the user record is missing, the service should throw the specific user-not-found message
        when(patientRepository.findByUserEmail("john.doe@example.com"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            patientService.updatePatientProfile(updateDto);
        });

        assertEquals("User not found while updating patient", exception.getMessage());
        verify(patientRepository).findByUserEmail("john.doe@example.com");
        verify(userRepository).findByEmail("john.doe@example.com");
        verify(userRepository, never()).save(any(User.class));
        verify(patientRepository, never()).save(any(Patient.class));
    }
}
