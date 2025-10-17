package com.lankamed.health.backend.service.patient;

import com.lankamed.health.backend.dto.patient.MedicalConditionDto;
import com.lankamed.health.backend.dto.patient.CreateMedicalConditionDto;
import com.lankamed.health.backend.dto.patient.AllergyDto;
import com.lankamed.health.backend.dto.patient.CreateAllergyDto;
import com.lankamed.health.backend.dto.patient.PrescriptionDto;
import com.lankamed.health.backend.repository.patient.PatientRepository;
import com.lankamed.health.backend.repository.patient.MedicalConditionRepository;
import com.lankamed.health.backend.repository.patient.AllergyRepository;
import com.lankamed.health.backend.repository.patient.PrescriptionRepository;

import com.lankamed.health.backend.model.User;
import com.lankamed.health.backend.model.patient.Patient;
import com.lankamed.health.backend.model.patient.MedicalCondition;
import com.lankamed.health.backend.model.patient.Allergy;
import com.lankamed.health.backend.model.patient.Prescription;
import com.lankamed.health.backend.model.StaffDetails;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalHistoryServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private MedicalConditionRepository medicalConditionRepository;

    @Mock
    private AllergyRepository allergyRepository;

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private MedicalHistoryService medicalHistoryService;

    private User testUser;
    private Patient testPatient;
    private MedicalCondition testCondition;
    private Allergy testAllergy;
    private Prescription testPrescription;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@realuser.com")
                .passwordHash("hashedPassword")
                .role(Role.PATIENT)
                .build();

        testPatient = Patient.builder()
                .patientId(1L)
                .user(testUser)
                .build();

        testCondition = MedicalCondition.builder()
                .conditionId(1L)
                .patient(testPatient)
                .conditionName("Diabetes")
                .diagnosedDate(LocalDate.of(2020, 1, 1))
                .notes("Type 2 diabetes")
                .build();

        testAllergy = Allergy.builder()
                .allergyId(1L)
                .patient(testPatient)
                .allergyName("Penicillin")
                .severity(Allergy.Severity.SEVERE)
                .notes("Causes severe reaction")
                .build();

        StaffDetails testDoctor = StaffDetails.builder()
                .staffId(2L)
                .user(User.builder()
                        .firstName("Dr. Jane")
                        .lastName("Smith")
                        .build())
                .specialization("Cardiology")
                .build();

        testPrescription = Prescription.builder()
                .prescriptionId(1L)
                .patient(testPatient)
                .doctor(testDoctor)
                .medicationName("Metformin")
                .dosage("500mg")
                .frequency("Twice daily")
                .startDate(LocalDate.of(2020, 1, 1))
                .endDate(LocalDate.of(2020, 12, 31))
                .build();

        // Mock SecurityContext
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        lenient().when(authentication.getName()).thenReturn("john.doe@realuser.com");
    }

    // Medical Conditions Tests
    @Test
    void getMedicalConditions_Success() {
        // Given
        when(medicalConditionRepository.findByPatientUserEmail("john.doe@realuser.com"))
                .thenReturn(Arrays.asList(testCondition));

        // When
        List<MedicalConditionDto> result = medicalHistoryService.getMedicalConditions();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Diabetes", result.get(0).getConditionName());
        assertEquals(LocalDate.of(2020, 1, 1), result.get(0).getDiagnosedDate());
        assertEquals("Type 2 diabetes", result.get(0).getNotes());
    }

    @Test
    void createMedicalCondition_Success() {
        // Given
        CreateMedicalConditionDto createDto = new CreateMedicalConditionDto();
        createDto.setConditionName("Hypertension");
        createDto.setDiagnosedDate(LocalDate.of(2021, 1, 1));
        createDto.setNotes("High blood pressure");

        when(patientRepository.findByUserEmail("john.doe@realuser.com"))
                .thenReturn(Optional.of(testPatient));
        when(medicalConditionRepository.save(any(MedicalCondition.class)))
                .thenReturn(testCondition);

        // When
        MedicalConditionDto result = medicalHistoryService.createMedicalCondition(createDto);

        // Then
        assertNotNull(result);
        verify(medicalConditionRepository).save(any(MedicalCondition.class));
    }

    @Test
    void updateMedicalCondition_Success() {
        // Given
        CreateMedicalConditionDto updateDto = new CreateMedicalConditionDto();
        updateDto.setConditionName("Updated Diabetes");
        updateDto.setNotes("Updated notes");

        when(medicalConditionRepository.findById(1L))
                .thenReturn(Optional.of(testCondition));
        when(medicalConditionRepository.save(any(MedicalCondition.class)))
                .thenReturn(testCondition);

        // When
        MedicalConditionDto result = medicalHistoryService.updateMedicalCondition(1L, updateDto);

        // Then
        assertNotNull(result);
        verify(medicalConditionRepository).save(any(MedicalCondition.class));
    }

    @Test
    void updateMedicalCondition_UnauthorizedAccess() {
        // Given
        User otherUser = User.builder().email("other@example.com").build();
        Patient otherPatient = Patient.builder().user(otherUser).build();
        MedicalCondition otherCondition = MedicalCondition.builder()
                .conditionId(2L)
                .patient(otherPatient)
                .build();

        CreateMedicalConditionDto updateDto = new CreateMedicalConditionDto();
        updateDto.setConditionName("Updated");

        when(medicalConditionRepository.findById(2L))
                .thenReturn(Optional.of(otherCondition));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            medicalHistoryService.updateMedicalCondition(2L, updateDto);
        });

        assertEquals("Unauthorized access to medical condition", exception.getMessage());
        verify(medicalConditionRepository, never()).save(any(MedicalCondition.class));
    }

    @Test
    void deleteMedicalCondition_Success() {
        // Given
        when(medicalConditionRepository.findById(1L))
                .thenReturn(Optional.of(testCondition));

        // When
        medicalHistoryService.deleteMedicalCondition(1L);

        // Then
        verify(medicalConditionRepository).delete(testCondition);
    }

    // Allergies Tests
    @Test
    void getAllergies_Success() {
        // Given
        when(allergyRepository.findByPatientUserEmail("john.doe@realuser.com"))
                .thenReturn(Arrays.asList(testAllergy));

        // When
        List<AllergyDto> result = medicalHistoryService.getAllergies();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Penicillin", result.get(0).getAllergyName());
        assertEquals(Allergy.Severity.SEVERE, result.get(0).getSeverity());
        assertEquals("Causes severe reaction", result.get(0).getNotes());
    }

    @Test
    void createAllergy_Success() {
        // Given
        CreateAllergyDto createDto = new CreateAllergyDto();
        createDto.setAllergyName("Peanuts");
        createDto.setSeverity(Allergy.Severity.MODERATE);
        createDto.setNotes("Causes mild reaction");

        when(patientRepository.findByUserEmail("john.doe@realuser.com"))
                .thenReturn(Optional.of(testPatient));
        when(allergyRepository.save(any(Allergy.class)))
                .thenReturn(testAllergy);

        // When
        AllergyDto result = medicalHistoryService.createAllergy(createDto);

        // Then
        assertNotNull(result);
        verify(allergyRepository).save(any(Allergy.class));
    }

    // Prescriptions Tests
    @Test
    void getPrescriptions_Success() {
        // Given
        when(patientRepository.findByUserEmail("john.doe@realuser.com"))
                .thenReturn(Optional.of(testPatient));
        when(prescriptionRepository.findByPatientPatientId(1L))
                .thenReturn(Arrays.asList(testPrescription));

        // When
        List<PrescriptionDto> result = medicalHistoryService.getPrescriptions();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Metformin", result.get(0).getMedicationName());
        assertEquals("500mg", result.get(0).getDosage());
        assertEquals("Twice daily", result.get(0).getFrequency());
        assertEquals("Dr. Jane Smith", result.get(0).getDoctorName());
        assertEquals("Cardiology", result.get(0).getDoctorSpecialization());
    }

    // Additional Medical Conditions Tests
    @Test
    void getMedicalConditions_EmptyList() {
        // Given
        when(medicalConditionRepository.findByPatientUserEmail("john.doe@realuser.com"))
                .thenReturn(Collections.emptyList());

        // When
        List<MedicalConditionDto> result = medicalHistoryService.getMedicalConditions();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void createMedicalCondition_PatientNotFound() {
        // Given
        CreateMedicalConditionDto createDto = new CreateMedicalConditionDto();
        createDto.setConditionName("Hypertension");

        when(patientRepository.findByUserEmail("john.doe@realuser.com"))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            medicalHistoryService.createMedicalCondition(createDto);
        });

        assertEquals("Patient not found", exception.getMessage());
        verify(medicalConditionRepository, never()).save(any(MedicalCondition.class));
    }

    @Test
    void updateMedicalCondition_NotFound() {
        // Given
        CreateMedicalConditionDto updateDto = new CreateMedicalConditionDto();
        updateDto.setConditionName("Updated");

        when(medicalConditionRepository.findById(999L))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            medicalHistoryService.updateMedicalCondition(999L, updateDto);
        });

        assertEquals("Medical condition not found", exception.getMessage());
        verify(medicalConditionRepository, never()).save(any(MedicalCondition.class));
    }

    @Test
    void deleteMedicalCondition_NotFound() {
        // Given
        when(medicalConditionRepository.findById(999L))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            medicalHistoryService.deleteMedicalCondition(999L);
        });

        assertEquals("Medical condition not found", exception.getMessage());
        verify(medicalConditionRepository, never()).delete(any(MedicalCondition.class));
    }

    @Test
    void deleteMedicalCondition_UnauthorizedAccess() {
        // Given
        User otherUser = User.builder().email("other@example.com").build();
        Patient otherPatient = Patient.builder().user(otherUser).build();
        MedicalCondition otherCondition = MedicalCondition.builder()
                .conditionId(2L)
                .patient(otherPatient)
                .build();

        when(medicalConditionRepository.findById(2L))
                .thenReturn(Optional.of(otherCondition));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            medicalHistoryService.deleteMedicalCondition(2L);
        });

        assertEquals("Unauthorized access to medical condition", exception.getMessage());
        verify(medicalConditionRepository, never()).delete(any(MedicalCondition.class));
    }

    // Additional Allergy Tests
    @Test
    void getAllergies_EmptyList() {
        // Given
        when(allergyRepository.findByPatientUserEmail("john.doe@realuser.com"))
                .thenReturn(Collections.emptyList());

        // When
        List<AllergyDto> result = medicalHistoryService.getAllergies();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void createAllergy_PatientNotFound() {
        // Given
        CreateAllergyDto createDto = new CreateAllergyDto();
        createDto.setAllergyName("Peanuts");
        createDto.setSeverity(Allergy.Severity.MODERATE);

        when(patientRepository.findByUserEmail("john.doe@realuser.com"))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            medicalHistoryService.createAllergy(createDto);
        });

        assertEquals("Patient not found", exception.getMessage());
        verify(allergyRepository, never()).save(any(Allergy.class));
    }

    @Test
    void updateAllergy_Success() {
        // Given
        CreateAllergyDto updateDto = new CreateAllergyDto();
        updateDto.setAllergyName("Updated Penicillin");
        updateDto.setSeverity(Allergy.Severity.MILD);
        updateDto.setNotes("Updated notes");

        when(allergyRepository.findById(1L))
                .thenReturn(Optional.of(testAllergy));
        when(allergyRepository.save(any(Allergy.class)))
                .thenReturn(testAllergy);

        // When
        AllergyDto result = medicalHistoryService.updateAllergy(1L, updateDto);

        // Then
        assertNotNull(result);
        verify(allergyRepository).save(any(Allergy.class));
    }

    @Test
    void updateAllergy_NotFound() {
        // Given
        CreateAllergyDto updateDto = new CreateAllergyDto();
        updateDto.setAllergyName("Updated");

        when(allergyRepository.findById(999L))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            medicalHistoryService.updateAllergy(999L, updateDto);
        });

        assertEquals("Allergy not found", exception.getMessage());
        verify(allergyRepository, never()).save(any(Allergy.class));
    }

    @Test
    void updateAllergy_UnauthorizedAccess() {
        // Given
        User otherUser = User.builder().email("other@example.com").build();
        Patient otherPatient = Patient.builder().user(otherUser).build();
        Allergy otherAllergy = Allergy.builder()
                .allergyId(2L)
                .patient(otherPatient)
                .build();

        CreateAllergyDto updateDto = new CreateAllergyDto();
        updateDto.setAllergyName("Updated");

        when(allergyRepository.findById(2L))
                .thenReturn(Optional.of(otherAllergy));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            medicalHistoryService.updateAllergy(2L, updateDto);
        });

        assertEquals("Unauthorized access to allergy", exception.getMessage());
        verify(allergyRepository, never()).save(any(Allergy.class));
    }

    @Test
    void deleteAllergy_Success() {
        // Given
        when(allergyRepository.findById(1L))
                .thenReturn(Optional.of(testAllergy));

        // When
        medicalHistoryService.deleteAllergy(1L);

        // Then
        verify(allergyRepository).delete(testAllergy);
    }

    @Test
    void deleteAllergy_NotFound() {
        // Given
        when(allergyRepository.findById(999L))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            medicalHistoryService.deleteAllergy(999L);
        });

        assertEquals("Allergy not found", exception.getMessage());
        verify(allergyRepository, never()).delete(any(Allergy.class));
    }

    @Test
    void deleteAllergy_UnauthorizedAccess() {
        // Given
        User otherUser = User.builder().email("other@example.com").build();
        Patient otherPatient = Patient.builder().user(otherUser).build();
        Allergy otherAllergy = Allergy.builder()
                .allergyId(2L)
                .patient(otherPatient)
                .build();

        when(allergyRepository.findById(2L))
                .thenReturn(Optional.of(otherAllergy));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            medicalHistoryService.deleteAllergy(2L);
        });

        assertEquals("Unauthorized access to allergy", exception.getMessage());
        verify(allergyRepository, never()).delete(any(Allergy.class));
    }

    // Additional Prescription Tests
    @Test
    void getPrescriptions_PatientNotFound() {
        // Given
        when(patientRepository.findByUserEmail("john.doe@realuser.com"))
                .thenReturn(Optional.empty());

        // When
        List<PrescriptionDto> result = medicalHistoryService.getPrescriptions();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(prescriptionRepository, never()).findByPatientPatientId(any(Long.class));
    }

    @Test
    void getPrescriptions_EmptyList() {
        // Given
        when(patientRepository.findByUserEmail("john.doe@realuser.com"))
                .thenReturn(Optional.of(testPatient));
        when(prescriptionRepository.findByPatientPatientId(1L))
                .thenReturn(Collections.emptyList());

        // When
        List<PrescriptionDto> result = medicalHistoryService.getPrescriptions();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getPrescriptions_WithNullDoctor() {
        // Given
        Prescription prescriptionWithNullDoctor = Prescription.builder()
                .prescriptionId(2L)
                .patient(testPatient)
                .doctor(null) // Null doctor
                .medicationName("Test Medication")
                .dosage("100mg")
                .frequency("Once daily")
                .startDate(LocalDate.of(2021, 1, 1))
                .endDate(LocalDate.of(2021, 12, 31))
                .build();

        when(patientRepository.findByUserEmail("john.doe@realuser.com"))
                .thenReturn(Optional.of(testPatient));
        when(prescriptionRepository.findByPatientPatientId(1L))
                .thenReturn(Arrays.asList(testPrescription, prescriptionWithNullDoctor));

        // When
        List<PrescriptionDto> result = medicalHistoryService.getPrescriptions();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size()); // Both prescriptions should be included
        assertEquals("Metformin", result.get(0).getMedicationName());
        assertEquals("Test Medication", result.get(1).getMedicationName());
        assertEquals("Unknown Doctor", result.get(1).getDoctorName());
        assertEquals("Unknown", result.get(1).getDoctorSpecialization());
    }

    @Test
    void getPrescriptions_WithDoctorHavingNullUser() {
        // Given
        StaffDetails doctorWithNullUser = StaffDetails.builder()
                .staffId(3L)
                .user(null) // Null user
                .specialization("Neurology")
                .build();

        Prescription prescriptionWithNullUser = Prescription.builder()
                .prescriptionId(3L)
                .patient(testPatient)
                .doctor(doctorWithNullUser)
                .medicationName("Test Medication 2")
                .dosage("200mg")
                .frequency("Twice daily")
                .startDate(LocalDate.of(2021, 1, 1))
                .endDate(LocalDate.of(2021, 12, 31))
                .build();

        when(patientRepository.findByUserEmail("john.doe@realuser.com"))
                .thenReturn(Optional.of(testPatient));
        when(prescriptionRepository.findByPatientPatientId(1L))
                .thenReturn(Arrays.asList(testPrescription, prescriptionWithNullUser));

        // When
        List<PrescriptionDto> result = medicalHistoryService.getPrescriptions();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Metformin", result.get(0).getMedicationName());
        assertEquals("Test Medication 2", result.get(1).getMedicationName());
        assertEquals("Unknown Doctor", result.get(1).getDoctorName());
        assertEquals("Neurology", result.get(1).getDoctorSpecialization());
    }

    @Test
    void getPrescriptions_WithDoctorHavingEmptyNames() {
        // Given
        User userWithEmptyNames = User.builder()
                .firstName("")
                .lastName("")
                .build();

        StaffDetails doctorWithEmptyNames = StaffDetails.builder()
                .staffId(4L)
                .user(userWithEmptyNames)
                .specialization("Dermatology")
                .build();

        Prescription prescriptionWithEmptyNames = Prescription.builder()
                .prescriptionId(4L)
                .patient(testPatient)
                .doctor(doctorWithEmptyNames)
                .medicationName("Test Medication 3")
                .dosage("300mg")
                .frequency("Three times daily")
                .startDate(LocalDate.of(2021, 1, 1))
                .endDate(LocalDate.of(2021, 12, 31))
                .build();

        when(patientRepository.findByUserEmail("john.doe@realuser.com"))
                .thenReturn(Optional.of(testPatient));
        when(prescriptionRepository.findByPatientPatientId(1L))
                .thenReturn(Arrays.asList(testPrescription, prescriptionWithEmptyNames));

        // When
        List<PrescriptionDto> result = medicalHistoryService.getPrescriptions();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Metformin", result.get(0).getMedicationName());
        assertEquals("Test Medication 3", result.get(1).getMedicationName());
        assertEquals("Unknown Doctor", result.get(1).getDoctorName());
        assertEquals("Dermatology", result.get(1).getDoctorSpecialization());
    }

    @Test
    void getPrescriptions_WithDoctorHavingNullSpecialization() {
        // Given
        StaffDetails doctorWithNullSpecialization = StaffDetails.builder()
                .staffId(5L)
                .user(User.builder()
                        .firstName("Dr. John")
                        .lastName("Doe")
                        .build())
                .specialization(null) // Null specialization
                .build();

        Prescription prescriptionWithNullSpecialization = Prescription.builder()
                .prescriptionId(5L)
                .patient(testPatient)
                .doctor(doctorWithNullSpecialization)
                .medicationName("Test Medication 4")
                .dosage("400mg")
                .frequency("Four times daily")
                .startDate(LocalDate.of(2021, 1, 1))
                .endDate(LocalDate.of(2021, 12, 31))
                .build();

        when(patientRepository.findByUserEmail("john.doe@realuser.com"))
                .thenReturn(Optional.of(testPatient));
        when(prescriptionRepository.findByPatientPatientId(1L))
                .thenReturn(Arrays.asList(testPrescription, prescriptionWithNullSpecialization));

        // When
        List<PrescriptionDto> result = medicalHistoryService.getPrescriptions();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Metformin", result.get(0).getMedicationName());
        assertEquals("Test Medication 4", result.get(1).getMedicationName());
        assertEquals("Dr. John Doe", result.get(1).getDoctorName());
        assertEquals("Unknown", result.get(1).getDoctorSpecialization());
    }

    // Test with all fields populated
    @Test
    void createMedicalCondition_WithAllFields() {
        // Given
        CreateMedicalConditionDto createDto = new CreateMedicalConditionDto();
        createDto.setConditionName("Hypertension");
        createDto.setDiagnosedDate(LocalDate.of(2021, 1, 1));
        createDto.setNotes("High blood pressure condition");

        when(patientRepository.findByUserEmail("john.doe@realuser.com"))
                .thenReturn(Optional.of(testPatient));
        when(medicalConditionRepository.save(any(MedicalCondition.class)))
                .thenAnswer(i -> i.getArgument(0));

        // When
        MedicalConditionDto result = medicalHistoryService.createMedicalCondition(createDto);

        // Then
        assertNotNull(result);
        assertEquals("Hypertension", result.getConditionName());
        assertEquals(LocalDate.of(2021, 1, 1), result.getDiagnosedDate());
        assertEquals("High blood pressure condition", result.getNotes());
        verify(medicalConditionRepository).save(any(MedicalCondition.class));
    }

    @Test
    void createAllergy_WithAllFields() {
        // Given
        CreateAllergyDto createDto = new CreateAllergyDto();
        createDto.setAllergyName("Shellfish");
        createDto.setSeverity(Allergy.Severity.SEVERE);
        createDto.setNotes("Causes anaphylactic shock");

        when(patientRepository.findByUserEmail("john.doe@realuser.com"))
                .thenReturn(Optional.of(testPatient));
        when(allergyRepository.save(any(Allergy.class)))
                .thenAnswer(i -> i.getArgument(0));

        // When
        AllergyDto result = medicalHistoryService.createAllergy(createDto);

        // Then
        assertNotNull(result);
        assertEquals("Shellfish", result.getAllergyName());
        assertEquals(Allergy.Severity.SEVERE, result.getSeverity());
        assertEquals("Causes anaphylactic shock", result.getNotes());
        verify(allergyRepository).save(any(Allergy.class));
    }

    @Test
    void updateMedicalCondition_WithAllFields() {
        // Given
        CreateMedicalConditionDto updateDto = new CreateMedicalConditionDto();
        updateDto.setConditionName("Updated Hypertension");
        updateDto.setDiagnosedDate(LocalDate.of(2022, 1, 1));
        updateDto.setNotes("Updated high blood pressure condition");

        when(medicalConditionRepository.findById(1L))
                .thenReturn(Optional.of(testCondition));
        when(medicalConditionRepository.save(any(MedicalCondition.class)))
                .thenAnswer(i -> i.getArgument(0));

        // When
        MedicalConditionDto result = medicalHistoryService.updateMedicalCondition(1L, updateDto);

        // Then
        assertNotNull(result);
        verify(medicalConditionRepository).save(any(MedicalCondition.class));
    }

    @Test
    void updateAllergy_WithAllFields() {
        // Given
        CreateAllergyDto updateDto = new CreateAllergyDto();
        updateDto.setAllergyName("Updated Shellfish");
        updateDto.setSeverity(Allergy.Severity.MODERATE);
        updateDto.setNotes("Updated allergy notes");

        when(allergyRepository.findById(1L))
                .thenReturn(Optional.of(testAllergy));
        when(allergyRepository.save(any(Allergy.class)))
                .thenAnswer(i -> i.getArgument(0));

        // When
        AllergyDto result = medicalHistoryService.updateAllergy(1L, updateDto);

        // Then
        assertNotNull(result);
        verify(allergyRepository).save(any(Allergy.class));
    }
}
