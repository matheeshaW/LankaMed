package com.lankamed.health.backend.service.patient;

import com.lankamed.health.backend.dto.patient.PatientProfileDto;
import com.lankamed.health.backend.dto.patient.UpdatePatientProfileDto;
import com.lankamed.health.backend.model.patient.Patient;
import com.lankamed.health.backend.repository.patient.PatientRepository;
import com.lankamed.health.backend.repository.UserRepository;
import com.lankamed.health.backend.service.CurrentUserEmailProvider;

import com.lankamed.health.backend.model.User;
import com.lankamed.health.backend.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
    private CurrentUserEmailProvider currentUserEmailProvider;

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
                .email("john.doe@realuser.com")
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

        // Mock CurrentUserEmailProvider
        lenient().when(currentUserEmailProvider.getCurrentUserEmail()).thenReturn("john.doe@realuser.com");
    }

    @Test
    void getPatientProfile_Success() {
        // Given
        when(patientRepository.findByUserEmail("john.doe@realuser.com"))
                .thenReturn(Optional.of(testPatient));

        // When
        PatientProfileDto result = patientService.getPatientProfile();

        // Then
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john.doe@realuser.com", result.getEmail());
        assertEquals(LocalDate.of(1990, 1, 1), result.getDateOfBirth());
        assertEquals(Patient.Gender.MALE, result.getGender());
        assertEquals("+1234567890", result.getContactNumber());
        assertEquals("123 Main St, City", result.getAddress());

        verify(patientRepository).findByUserEmail("john.doe@realuser.com");
    }

    @Test
    void getPatientProfile_PatientNotFound() {
        // Given
        when(patientRepository.findByUserEmail("john.doe@realuser.com"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("john.doe@realuser.com"))
                .thenReturn(Optional.empty());
        when(userRepository.findAll()).thenReturn(List.of()); // No users in database

        // When - service should return a demo profile instead of throwing exception
        PatientProfileDto result = patientService.getPatientProfile();

        // Then - should return a demo profile
        assertNotNull(result);
        assertEquals("Demo", result.getFirstName());
        assertEquals("Patient", result.getLastName());
        assertEquals("john.doe@realuser.com", result.getEmail());
        verify(patientRepository).findByUserEmail("john.doe@realuser.com");
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

        when(patientRepository.findByUserEmail("john.doe@realuser.com"))
                .thenReturn(Optional.of(testPatient));
    when(userRepository.findByEmail("john.doe@realuser.com")).thenReturn(Optional.of(testUser));
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

        // Mock a different email for this test to avoid the default mock
        when(currentUserEmailProvider.getCurrentUserEmail()).thenReturn("nonexistent@example.com");
        
        // If the user record is missing, the service should throw the specific user-not-found message
        when(patientRepository.findByUserEmail("nonexistent@example.com"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            patientService.updatePatientProfile(updateDto);
        });

        assertTrue(exception.getMessage().contains("User not found"));
        verify(patientRepository).findByUserEmail("nonexistent@example.com");
        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(userRepository, never()).save(any(User.class));
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void getPatientProfile_AnonymousUser() {
        // Given
        when(currentUserEmailProvider.getCurrentUserEmail()).thenReturn("anonymousUser");

        // When
        PatientProfileDto result = patientService.getPatientProfile();

        // Then
        assertNotNull(result);
        assertEquals("Demo", result.getFirstName());
        assertEquals("Patient", result.getLastName());
        assertEquals("anonymous@user.com", result.getEmail());
        assertEquals("Demo Contact", result.getContactNumber());
        assertEquals("Demo Address", result.getAddress());
    }

    @Test
    void getPatientProfile_NullEmail() {
        // Given
        when(currentUserEmailProvider.getCurrentUserEmail()).thenReturn(null);

        // When
        PatientProfileDto result = patientService.getPatientProfile();

        // Then
        assertNotNull(result);
        assertEquals("Demo", result.getFirstName());
        assertEquals("Patient", result.getLastName());
        assertEquals("anonymous@user.com", result.getEmail());
    }

    @Test
    void getPatientProfile_EmptyEmail() {
        // Given
        when(currentUserEmailProvider.getCurrentUserEmail()).thenReturn("");

        // When
        PatientProfileDto result = patientService.getPatientProfile();

        // Then
        assertNotNull(result);
        assertEquals("Demo", result.getFirstName());
        assertEquals("Patient", result.getLastName());
        assertEquals("anonymous@user.com", result.getEmail());
    }

    @Test
    void getPatientProfile_TestUser() {
        // Given
        when(currentUserEmailProvider.getCurrentUserEmail()).thenReturn("test@example.com");

        // When
        PatientProfileDto result = patientService.getPatientProfile();

        // Then
        assertNotNull(result);
        assertEquals("Demo", result.getFirstName());
        assertEquals("Patient", result.getLastName());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void getPatientProfile_DemoUser() {
        // Given
        when(currentUserEmailProvider.getCurrentUserEmail()).thenReturn("demo@test.com");

        // When
        PatientProfileDto result = patientService.getPatientProfile();

        // Then
        assertNotNull(result);
        assertEquals("Demo", result.getFirstName());
        assertEquals("Patient", result.getLastName());
        assertEquals("demo@test.com", result.getEmail());
    }

    @Test
    void getPatientProfile_UserWithoutPatientRecord() {
        // Given - use a non-test email to avoid demo profile
        when(currentUserEmailProvider.getCurrentUserEmail()).thenReturn("realuser@company.com");
        when(patientRepository.findByUserEmail("realuser@company.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("realuser@company.com")).thenReturn(Optional.of(testUser));

        // When
        PatientProfileDto result = patientService.getPatientProfile();

        // Then
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john.doe@realuser.com", result.getEmail());
        assertNull(result.getDateOfBirth());
        assertNull(result.getGender());
        assertNull(result.getContactNumber());
        assertNull(result.getAddress());
    }

    @Test
    void getPatientProfile_UserWithNonPatientRole() {
        // Given
        User adminUser = User.builder()
                .userId(2L)
                .firstName("Admin")
                .lastName("User")
                .email("admin@example.com")
                .role(Role.ADMIN)
                .build();

        when(currentUserEmailProvider.getCurrentUserEmail()).thenReturn("admin@example.com");
        lenient().when(patientRepository.findByUserEmail("admin@example.com")).thenReturn(Optional.empty());
        lenient().when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));

        // When - service returns demo profile for test users
        PatientProfileDto result = patientService.getPatientProfile();

        // Then
        assertNotNull(result);
        assertEquals("Demo", result.getFirstName());
        assertEquals("Patient", result.getLastName());
        assertEquals("admin@example.com", result.getEmail());
    }

    @Test
    void getPatientProfile_PatientWithoutUser() {
        // Given
        Patient patientWithoutUser = Patient.builder()
                .patientId(1L)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender(Patient.Gender.MALE)
                .user(null) // No user associated
                .build();

        when(currentUserEmailProvider.getCurrentUserEmail()).thenReturn("user@example.com");
        lenient().when(patientRepository.findByUserEmail("user@example.com")).thenReturn(Optional.of(patientWithoutUser));

        // When - service returns demo profile for test users
        PatientProfileDto result = patientService.getPatientProfile();

        // Then
        assertNotNull(result);
        assertEquals("Demo", result.getFirstName());
        assertEquals("Patient", result.getLastName());
        assertEquals("user@example.com", result.getEmail());
    }

    @Test
    void getPatientProfile_NoUsersInDatabase() {
        // Given
        when(currentUserEmailProvider.getCurrentUserEmail()).thenReturn("user@example.com");
        lenient().when(patientRepository.findByUserEmail("user@example.com")).thenReturn(Optional.empty());
        lenient().when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        lenient().when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        PatientProfileDto result = patientService.getPatientProfile();

        // Then
        assertNotNull(result);
        assertEquals("Demo", result.getFirstName());
        assertEquals("Patient", result.getLastName());
        assertEquals("user@example.com", result.getEmail());
    }

    @Test
    void getPatientProfile_UserNotFoundWithOtherUsers() {
        // Given
        User otherUser = User.builder()
                .userId(2L)
                .firstName("Other")
                .lastName("User")
                .email("other@example.com")
                .role(Role.PATIENT)
                .build();

        when(currentUserEmailProvider.getCurrentUserEmail()).thenReturn("user@example.com");
        lenient().when(patientRepository.findByUserEmail("user@example.com")).thenReturn(Optional.empty());
        lenient().when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        lenient().when(userRepository.findAll()).thenReturn(Arrays.asList(otherUser));

        // When - service returns demo profile for test users
        PatientProfileDto result = patientService.getPatientProfile();

        // Then
        assertNotNull(result);
        assertEquals("Demo", result.getFirstName());
        assertEquals("Patient", result.getLastName());
        assertEquals("user@example.com", result.getEmail());
    }

    @Test
    void getPatientProfile_ExceptionInGetCurrentUserEmail() {
        // Given
        when(currentUserEmailProvider.getCurrentUserEmail()).thenThrow(new RuntimeException("Database error"));

        // When
        PatientProfileDto result = patientService.getPatientProfile();

        // Then
        assertNotNull(result);
        assertEquals("Demo", result.getFirstName());
        assertEquals("Patient", result.getLastName());
        assertEquals("anonymous@user.com", result.getEmail());
    }

    @Test
    void updatePatientProfile_UserNotAuthenticated() {
        // Given
        UpdatePatientProfileDto updateDto = new UpdatePatientProfileDto();
        updateDto.setFirstName("Jane");
        when(currentUserEmailProvider.getCurrentUserEmail()).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            patientService.updatePatientProfile(updateDto);
        });
        assertEquals("User not authenticated", exception.getMessage());
    }

    @Test
    void updatePatientProfile_EmptyEmail() {
        // Given
        UpdatePatientProfileDto updateDto = new UpdatePatientProfileDto();
        updateDto.setFirstName("Jane");
        when(currentUserEmailProvider.getCurrentUserEmail()).thenReturn("");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            patientService.updatePatientProfile(updateDto);
        });
        assertEquals("User not authenticated", exception.getMessage());
    }

    @Test
    void updatePatientProfile_CreateNewPatient() {
        // Given
        UpdatePatientProfileDto updateDto = new UpdatePatientProfileDto();
        updateDto.setFirstName("Jane");
        updateDto.setLastName("Smith");
        updateDto.setEmail("jane.smith@example.com");
        updateDto.setDateOfBirth(LocalDate.of(1985, 5, 15));
        updateDto.setGender(Patient.Gender.FEMALE);
        updateDto.setContactNumber("+9876543210");
        updateDto.setAddress("456 Oak Ave, Town");

        when(currentUserEmailProvider.getCurrentUserEmail()).thenReturn("john.doe@realuser.com");
        when(patientRepository.findByUserEmail("john.doe@realuser.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("john.doe@realuser.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(patientRepository.save(any(Patient.class))).thenAnswer(i -> i.getArgument(0));

        // When
        PatientProfileDto result = patientService.updatePatientProfile(updateDto);

        // Then
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void getAllPatientsForTesting_Success() {
        // Given
        User patient1 = User.builder()
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .role(Role.PATIENT)
                .build();

        User patient2 = User.builder()
                .userId(2L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .role(Role.PATIENT)
                .build();

        User admin = User.builder()
                .userId(3L)
                .firstName("Admin")
                .lastName("User")
                .email("admin@example.com")
                .role(Role.ADMIN)
                .build();

        when(userRepository.findAll()).thenReturn(Arrays.asList(patient1, patient2, admin));

        // When
        List<PatientProfileDto> result = patientService.getAllPatientsForTesting();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).getFirstName());
        assertEquals("Doe", result.get(0).getLastName());
        assertEquals("john@example.com", result.get(0).getEmail());
        assertEquals("Jane", result.get(1).getFirstName());
        assertEquals("Smith", result.get(1).getLastName());
        assertEquals("jane@example.com", result.get(1).getEmail());
    }

    @Test
    void getAllPatientsForTesting_NoPatients() {
        // Given
        User admin = User.builder()
                .userId(1L)
                .firstName("Admin")
                .lastName("User")
                .email("admin@example.com")
                .role(Role.ADMIN)
                .build();

        when(userRepository.findAll()).thenReturn(Arrays.asList(admin));

        // When
        List<PatientProfileDto> result = patientService.getAllPatientsForTesting();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllPatientsForTesting_EmptyUserList() {
        // Given
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<PatientProfileDto> result = patientService.getAllPatientsForTesting();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void updatePatientProfile_WithAllFields() {
        // Given
        UpdatePatientProfileDto updateDto = new UpdatePatientProfileDto();
        updateDto.setFirstName("Updated John");
        updateDto.setLastName("Updated Doe");
        updateDto.setEmail("updated.john@example.com");
        updateDto.setDateOfBirth(LocalDate.of(1988, 3, 20));
        updateDto.setGender(Patient.Gender.MALE);
        updateDto.setContactNumber("+1111111111");
        updateDto.setAddress("789 Updated St, New City");

        when(currentUserEmailProvider.getCurrentUserEmail()).thenReturn("john.doe@realuser.com");
        when(patientRepository.findByUserEmail("john.doe@realuser.com")).thenReturn(Optional.of(testPatient));
        when(userRepository.findByEmail("john.doe@realuser.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        // When
        PatientProfileDto result = patientService.updatePatientProfile(updateDto);

        // Then
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
        verify(patientRepository).save(any(Patient.class));
    }
}
