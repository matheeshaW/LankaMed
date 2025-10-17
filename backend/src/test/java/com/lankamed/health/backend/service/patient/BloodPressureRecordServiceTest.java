package com.lankamed.health.backend.service.patient;

import com.lankamed.health.backend.dto.patient.BloodPressureRecordDto;
import com.lankamed.health.backend.model.patient.BloodPressureRecord;
import com.lankamed.health.backend.model.patient.Patient;
import com.lankamed.health.backend.model.User;
import com.lankamed.health.backend.repository.patient.BloodPressureRecordRepository;
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
class BloodPressureRecordServiceTest {

    @Mock
    private BloodPressureRecordRepository bpRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private BloodPressureRecordService bpService;

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
        BloodPressureRecordDto dto = BloodPressureRecordDto.builder()
            .systolic(120)
            .diastolic(80)
            .timestamp(LocalDateTime.now()) // changed: use LocalDateTime
            .build();
        when(patientRepository.findByUserEmail("p@example.com")).thenReturn(Optional.of(testPatient));
        when(bpRepository.save(any(BloodPressureRecord.class))).thenAnswer(i -> i.getArgument(0));

        BloodPressureRecordDto res = bpService.addRecord(dto);
        assertNotNull(res);
        assertEquals(120, res.getSystolic());
        verify(bpRepository).save(any(BloodPressureRecord.class));
    }

    @Test
    void getAllRecordsForCurrentPatient_Success() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime earlier = now.minusHours(2);
        
        BloodPressureRecord record1 = BloodPressureRecord.builder()
                .id(1L)
                .patient(testPatient)
                .systolic(120)
                .diastolic(80)
                .timestamp(now)
                .build();
        
        BloodPressureRecord record2 = BloodPressureRecord.builder()
                .id(2L)
                .patient(testPatient)
                .systolic(115)
                .diastolic(75)
                .timestamp(earlier)
                .build();

        when(bpRepository.findByPatientUserEmail("p@example.com"))
                .thenReturn(Arrays.asList(record1, record2));

        // When
        List<BloodPressureRecordDto> result = bpService.getAllRecordsForCurrentPatient();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        // Should be sorted by timestamp (earliest first)
        assertEquals(115, result.get(0).getSystolic());
        assertEquals(75, result.get(0).getDiastolic());
        assertEquals(earlier, result.get(0).getTimestamp());
        assertEquals(120, result.get(1).getSystolic());
        assertEquals(80, result.get(1).getDiastolic());
        assertEquals(now, result.get(1).getTimestamp());
    }

    @Test
    void getAllRecordsForCurrentPatient_EmptyList() {
        // Given
        when(bpRepository.findByPatientUserEmail("p@example.com"))
                .thenReturn(Collections.emptyList());

        // When
        List<BloodPressureRecordDto> result = bpService.getAllRecordsForCurrentPatient();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllRecordsForCurrentPatient_SingleRecord() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        BloodPressureRecord record = BloodPressureRecord.builder()
                .id(1L)
                .patient(testPatient)
                .systolic(125)
                .diastolic(85)
                .timestamp(now)
                .build();

        when(bpRepository.findByPatientUserEmail("p@example.com"))
                .thenReturn(Arrays.asList(record));

        // When
        List<BloodPressureRecordDto> result = bpService.getAllRecordsForCurrentPatient();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(125, result.get(0).getSystolic());
        assertEquals(85, result.get(0).getDiastolic());
        assertEquals(now, result.get(0).getTimestamp());
    }

    @Test
    void addRecord_PatientNotFound() {
        // Given
        BloodPressureRecordDto dto = BloodPressureRecordDto.builder()
                .systolic(120)
                .diastolic(80)
                .timestamp(LocalDateTime.now())
                .build();

        when(patientRepository.findByUserEmail("p@example.com"))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bpService.addRecord(dto);
        });

        assertEquals("Patient not found", exception.getMessage());
        verify(bpRepository, never()).save(any(BloodPressureRecord.class));
    }

    @Test
    void addRecord_WithAllFields() {
        // Given
        LocalDateTime timestamp = LocalDateTime.of(2023, 12, 25, 16, 45);
        BloodPressureRecordDto dto = BloodPressureRecordDto.builder()
                .systolic(118)
                .diastolic(78)
                .timestamp(timestamp)
                .build();

        when(patientRepository.findByUserEmail("p@example.com"))
                .thenReturn(Optional.of(testPatient));
        when(bpRepository.save(any(BloodPressureRecord.class)))
                .thenAnswer(i -> i.getArgument(0));

        // When
        BloodPressureRecordDto result = bpService.addRecord(dto);

        // Then
        assertNotNull(result);
        assertEquals(118, result.getSystolic());
        assertEquals(78, result.getDiastolic());
        assertEquals(timestamp, result.getTimestamp());
        verify(bpRepository).save(any(BloodPressureRecord.class));
    }

    @Test
    void getAllRecordsForCurrentPatient_MultipleRecordsSorted() {
        // Given
        LocalDateTime baseTime = LocalDateTime.of(2023, 1, 1, 12, 0);
        
        BloodPressureRecord record1 = BloodPressureRecord.builder()
                .id(1L)
                .patient(testPatient)
                .systolic(130)
                .diastolic(90)
                .timestamp(baseTime.plusHours(3)) // Latest
                .build();
        
        BloodPressureRecord record2 = BloodPressureRecord.builder()
                .id(2L)
                .patient(testPatient)
                .systolic(120)
                .diastolic(80)
                .timestamp(baseTime) // Earliest
                .build();
        
        BloodPressureRecord record3 = BloodPressureRecord.builder()
                .id(3L)
                .patient(testPatient)
                .systolic(125)
                .diastolic(85)
                .timestamp(baseTime.plusHours(1)) // Middle
                .build();

        // Return in random order to test sorting
        when(bpRepository.findByPatientUserEmail("p@example.com"))
                .thenReturn(Arrays.asList(record1, record2, record3));

        // When
        List<BloodPressureRecordDto> result = bpService.getAllRecordsForCurrentPatient();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        // Should be sorted by timestamp (earliest first)
        assertEquals(120, result.get(0).getSystolic());
        assertEquals(80, result.get(0).getDiastolic());
        assertEquals(baseTime, result.get(0).getTimestamp());
        assertEquals(125, result.get(1).getSystolic());
        assertEquals(85, result.get(1).getDiastolic());
        assertEquals(baseTime.plusHours(1), result.get(1).getTimestamp());
        assertEquals(130, result.get(2).getSystolic());
        assertEquals(90, result.get(2).getDiastolic());
        assertEquals(baseTime.plusHours(3), result.get(2).getTimestamp());
    }

    @Test
    void addRecord_LowBloodPressure() {
        // Given
        BloodPressureRecordDto dto = BloodPressureRecordDto.builder()
                .systolic(90)
                .diastolic(60)
                .timestamp(LocalDateTime.now())
                .build();

        when(patientRepository.findByUserEmail("p@example.com"))
                .thenReturn(Optional.of(testPatient));
        when(bpRepository.save(any(BloodPressureRecord.class)))
                .thenAnswer(i -> i.getArgument(0));

        // When
        BloodPressureRecordDto result = bpService.addRecord(dto);

        // Then
        assertNotNull(result);
        assertEquals(90, result.getSystolic());
        assertEquals(60, result.getDiastolic());
        verify(bpRepository).save(any(BloodPressureRecord.class));
    }

    @Test
    void addRecord_HighBloodPressure() {
        // Given
        BloodPressureRecordDto dto = BloodPressureRecordDto.builder()
                .systolic(180)
                .diastolic(120)
                .timestamp(LocalDateTime.now())
                .build();

        when(patientRepository.findByUserEmail("p@example.com"))
                .thenReturn(Optional.of(testPatient));
        when(bpRepository.save(any(BloodPressureRecord.class)))
                .thenAnswer(i -> i.getArgument(0));

        // When
        BloodPressureRecordDto result = bpService.addRecord(dto);

        // Then
        assertNotNull(result);
        assertEquals(180, result.getSystolic());
        assertEquals(120, result.getDiastolic());
        verify(bpRepository).save(any(BloodPressureRecord.class));
    }

    @Test
    void addRecord_NormalBloodPressure() {
        // Given
        BloodPressureRecordDto dto = BloodPressureRecordDto.builder()
                .systolic(110)
                .diastolic(70)
                .timestamp(LocalDateTime.now())
                .build();

        when(patientRepository.findByUserEmail("p@example.com"))
                .thenReturn(Optional.of(testPatient));
        when(bpRepository.save(any(BloodPressureRecord.class)))
                .thenAnswer(i -> i.getArgument(0));

        // When
        BloodPressureRecordDto result = bpService.addRecord(dto);

        // Then
        assertNotNull(result);
        assertEquals(110, result.getSystolic());
        assertEquals(70, result.getDiastolic());
        verify(bpRepository).save(any(BloodPressureRecord.class));
    }

    @Test
    void getAllRecordsForCurrentPatient_WithSameTimestamp() {
        // Given
        LocalDateTime sameTime = LocalDateTime.of(2023, 1, 1, 12, 0);
        
        BloodPressureRecord record1 = BloodPressureRecord.builder()
                .id(1L)
                .patient(testPatient)
                .systolic(120)
                .diastolic(80)
                .timestamp(sameTime)
                .build();
        
        BloodPressureRecord record2 = BloodPressureRecord.builder()
                .id(2L)
                .patient(testPatient)
                .systolic(125)
                .diastolic(85)
                .timestamp(sameTime) // Same timestamp
                .build();

        when(bpRepository.findByPatientUserEmail("p@example.com"))
                .thenReturn(Arrays.asList(record1, record2));

        // When
        List<BloodPressureRecordDto> result = bpService.getAllRecordsForCurrentPatient();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        // Both records should be present (stable sort)
        assertEquals(sameTime, result.get(0).getTimestamp());
        assertEquals(sameTime, result.get(1).getTimestamp());
    }
}
