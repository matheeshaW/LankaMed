package com.lankamed.health.backend.service.patient;

import com.lankamed.health.backend.dto.patient.WeightRecordDto;
import com.lankamed.health.backend.model.patient.WeightRecord;
import com.lankamed.health.backend.model.patient.Patient;
import com.lankamed.health.backend.model.User;
import com.lankamed.health.backend.repository.patient.WeightRecordRepository;
import com.lankamed.health.backend.repository.patient.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;


import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeightRecordServiceTest {

    @Mock
    private WeightRecordRepository weightRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private WeightRecordService weightRecordService;

    private Patient testPatient;

    @BeforeEach
    void setUp() {
        User u = User.builder().userId(1L).email("p@example.com").build();
        testPatient = Patient.builder().patientId(1L).user(u).build();

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        lenient().when(authentication.getName()).thenReturn("p@example.com");
    }

    @Test
    void addRecord_Success() {
        WeightRecordDto dto = WeightRecordDto.builder()
            .weightKg(70.5)
            .timestamp(LocalDateTime.now()) // changed: use LocalDateTime
            .build();
        when(patientRepository.findByUserEmail("p@example.com")).thenReturn(Optional.of(testPatient));
        when(weightRepository.save(any(WeightRecord.class))).thenAnswer(i -> i.getArgument(0));

        WeightRecordDto res = weightRecordService.addRecord(dto);
        assertNotNull(res);
        assertEquals(70.5, res.getWeightKg());
        verify(weightRepository).save(any(WeightRecord.class));
    }

    @Test
    void getAllRecordsForCurrentPatient_Success() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime earlier = now.minusDays(1);
        
        WeightRecord record1 = WeightRecord.builder()
                .id(1L)
                .patient(testPatient)
                .weightKg(70.5)
                .timestamp(now)
                .build();
        
        WeightRecord record2 = WeightRecord.builder()
                .id(2L)
                .patient(testPatient)
                .weightKg(69.8)
                .timestamp(earlier)
                .build();

        when(weightRepository.findByPatientUserEmail("p@example.com"))
                .thenReturn(Arrays.asList(record1, record2));

        // When
        List<WeightRecordDto> result = weightRecordService.getAllRecordsForCurrentPatient();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        // Should be sorted by timestamp (earliest first)
        assertEquals(69.8, result.get(0).getWeightKg());
        assertEquals(70.5, result.get(1).getWeightKg());
        assertEquals(earlier, result.get(0).getTimestamp());
        assertEquals(now, result.get(1).getTimestamp());
    }

    @Test
    void getAllRecordsForCurrentPatient_EmptyList() {
        // Given
        when(weightRepository.findByPatientUserEmail("p@example.com"))
                .thenReturn(Collections.emptyList());

        // When
        List<WeightRecordDto> result = weightRecordService.getAllRecordsForCurrentPatient();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllRecordsForCurrentPatient_SingleRecord() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        WeightRecord record = WeightRecord.builder()
                .id(1L)
                .patient(testPatient)
                .weightKg(75.2)
                .timestamp(now)
                .build();

        when(weightRepository.findByPatientUserEmail("p@example.com"))
                .thenReturn(Arrays.asList(record));

        // When
        List<WeightRecordDto> result = weightRecordService.getAllRecordsForCurrentPatient();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(75.2, result.get(0).getWeightKg());
        assertEquals(now, result.get(0).getTimestamp());
    }

    @Test
    void addRecord_PatientNotFound() {
        // Given
        WeightRecordDto dto = WeightRecordDto.builder()
                .weightKg(70.5)
                .timestamp(LocalDateTime.now())
                .build();

        when(patientRepository.findByUserEmail("p@example.com"))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            weightRecordService.addRecord(dto);
        });

        assertEquals("Patient not found", exception.getMessage());
        verify(weightRepository, never()).save(any(WeightRecord.class));
    }

    @Test
    void addRecord_WithAllFields() {
        // Given
        LocalDateTime timestamp = LocalDateTime.of(2023, 12, 25, 10, 30);
        WeightRecordDto dto = WeightRecordDto.builder()
                .weightKg(68.3)
                .timestamp(timestamp)
                .build();

        when(patientRepository.findByUserEmail("p@example.com"))
                .thenReturn(Optional.of(testPatient));
        when(weightRepository.save(any(WeightRecord.class)))
                .thenAnswer(i -> i.getArgument(0));

        // When
        WeightRecordDto result = weightRecordService.addRecord(dto);

        // Then
        assertNotNull(result);
        assertEquals(68.3, result.getWeightKg());
        assertEquals(timestamp, result.getTimestamp());
        verify(weightRepository).save(any(WeightRecord.class));
    }

    @Test
    void getAllRecordsForCurrentPatient_MultipleRecordsSorted() {
        // Given
        LocalDateTime baseTime = LocalDateTime.of(2023, 1, 1, 12, 0);
        
        WeightRecord record1 = WeightRecord.builder()
                .id(1L)
                .patient(testPatient)
                .weightKg(70.0)
                .timestamp(baseTime.plusDays(2)) // Latest
                .build();
        
        WeightRecord record2 = WeightRecord.builder()
                .id(2L)
                .patient(testPatient)
                .weightKg(69.5)
                .timestamp(baseTime) // Earliest
                .build();
        
        WeightRecord record3 = WeightRecord.builder()
                .id(3L)
                .patient(testPatient)
                .weightKg(69.8)
                .timestamp(baseTime.plusDays(1)) // Middle
                .build();

        // Return in random order to test sorting
        when(weightRepository.findByPatientUserEmail("p@example.com"))
                .thenReturn(Arrays.asList(record1, record2, record3));

        // When
        List<WeightRecordDto> result = weightRecordService.getAllRecordsForCurrentPatient();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        // Should be sorted by timestamp (earliest first)
        assertEquals(69.5, result.get(0).getWeightKg());
        assertEquals(baseTime, result.get(0).getTimestamp());
        assertEquals(69.8, result.get(1).getWeightKg());
        assertEquals(baseTime.plusDays(1), result.get(1).getTimestamp());
        assertEquals(70.0, result.get(2).getWeightKg());
        assertEquals(baseTime.plusDays(2), result.get(2).getTimestamp());
    }

    @Test
    void addRecord_ZeroWeight() {
        // Given
        WeightRecordDto dto = WeightRecordDto.builder()
                .weightKg(0.0)
                .timestamp(LocalDateTime.now())
                .build();

        when(patientRepository.findByUserEmail("p@example.com"))
                .thenReturn(Optional.of(testPatient));
        when(weightRepository.save(any(WeightRecord.class)))
                .thenAnswer(i -> i.getArgument(0));

        // When
        WeightRecordDto result = weightRecordService.addRecord(dto);

        // Then
        assertNotNull(result);
        assertEquals(0.0, result.getWeightKg());
        verify(weightRepository).save(any(WeightRecord.class));
    }

    @Test
    void addRecord_HighWeight() {
        // Given
        WeightRecordDto dto = WeightRecordDto.builder()
                .weightKg(200.5)
                .timestamp(LocalDateTime.now())
                .build();

        when(patientRepository.findByUserEmail("p@example.com"))
                .thenReturn(Optional.of(testPatient));
        when(weightRepository.save(any(WeightRecord.class)))
                .thenAnswer(i -> i.getArgument(0));

        // When
        WeightRecordDto result = weightRecordService.addRecord(dto);

        // Then
        assertNotNull(result);
        assertEquals(200.5, result.getWeightKg());
        verify(weightRepository).save(any(WeightRecord.class));
    }
}
