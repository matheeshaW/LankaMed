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
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("john.doe@realuser.com");
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
}
