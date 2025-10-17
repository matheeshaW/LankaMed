package com.lankamed.health.backend.service.patient;

import com.lankamed.health.backend.dto.patient.HealthMetricDto;
import com.lankamed.health.backend.model.patient.HealthMetric;
import com.lankamed.health.backend.model.patient.Patient;
import com.lankamed.health.backend.model.User;
import com.lankamed.health.backend.repository.patient.HealthMetricRepository;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthMetricServiceTest {

    @Mock
    private HealthMetricRepository healthMetricRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private HealthMetricService healthMetricService;

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
    void getLatestMetric_ReturnsMaxTimestamp() {
        HealthMetric a = HealthMetric.builder()
            .systolic(120)
            .diastolic(80)
            .timestamp(LocalDateTime.ofInstant(Instant.parse("2023-01-01T00:00:00Z"), ZoneId.systemDefault()))
            .build();
        HealthMetric b = HealthMetric.builder()
            .systolic(130)
            .diastolic(85)
            .timestamp(LocalDateTime.ofInstant(Instant.parse("2024-01-01T00:00:00Z"), ZoneId.systemDefault()))
            .build();
        when(healthMetricRepository.findByPatientUserEmail("p@example.com")).thenReturn(Arrays.asList(a, b));

        Optional<HealthMetricDto> opt = healthMetricService.getLatestMetric();
        assertTrue(opt.isPresent());
        assertEquals(130, opt.get().getSystolic());
    }

    @Test
    void addMetric_Success() {
        HealthMetricDto dto = HealthMetricDto.builder()
            .systolic(110)
            .diastolic(70)
            .timestamp(LocalDateTime.now()) // use LocalDateTime here
            .build();
        when(patientRepository.findByUserEmail("p@example.com")).thenReturn(Optional.of(testPatient));
        when(healthMetricRepository.save(any(HealthMetric.class))).thenAnswer(i -> i.getArgument(0));

        HealthMetricDto res = healthMetricService.addMetric(dto);
        assertNotNull(res);
    }

    @Test
    void getMetricsForCurrentPatient_Success() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime earlier = now.minusHours(1);
        
        HealthMetric metric1 = HealthMetric.builder()
                .id(1L)
                .patient(testPatient)
                .systolic(120)
                .diastolic(80)
                .heartRate(72)
                .spo2(98)
                .timestamp(now)
                .build();
        
        HealthMetric metric2 = HealthMetric.builder()
                .id(2L)
                .patient(testPatient)
                .systolic(115)
                .diastolic(75)
                .heartRate(68)
                .spo2(99)
                .timestamp(earlier)
                .build();

        when(healthMetricRepository.findByPatientUserEmail("p@example.com"))
                .thenReturn(Arrays.asList(metric1, metric2));

        // When
        List<HealthMetricDto> result = healthMetricService.getMetricsForCurrentPatient();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(120, result.get(0).getSystolic());
        assertEquals(80, result.get(0).getDiastolic());
        assertEquals(72, result.get(0).getHeartRate());
        assertEquals(98, result.get(0).getSpo2());
        assertEquals(now, result.get(0).getTimestamp());
    }

    @Test
    void getMetricsForCurrentPatient_EmptyList() {
        // Given
        when(healthMetricRepository.findByPatientUserEmail("p@example.com"))
                .thenReturn(Collections.emptyList());

        // When
        List<HealthMetricDto> result = healthMetricService.getMetricsForCurrentPatient();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getMetricsForCurrentPatient_SingleMetric() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        HealthMetric metric = HealthMetric.builder()
                .id(1L)
                .patient(testPatient)
                .systolic(110)
                .diastolic(70)
                .heartRate(65)
                .spo2(97)
                .timestamp(now)
                .build();

        when(healthMetricRepository.findByPatientUserEmail("p@example.com"))
                .thenReturn(Arrays.asList(metric));

        // When
        List<HealthMetricDto> result = healthMetricService.getMetricsForCurrentPatient();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(110, result.get(0).getSystolic());
        assertEquals(70, result.get(0).getDiastolic());
        assertEquals(65, result.get(0).getHeartRate());
        assertEquals(97, result.get(0).getSpo2());
        assertEquals(now, result.get(0).getTimestamp());
    }

    @Test
    void getLatestMetric_EmptyList() {
        // Given
        when(healthMetricRepository.findByPatientUserEmail("p@example.com"))
                .thenReturn(Collections.emptyList());

        // When
        Optional<HealthMetricDto> result = healthMetricService.getLatestMetric();

        // Then
        assertNotNull(result);
        assertFalse(result.isPresent());
    }

    @Test
    void getLatestMetric_SingleMetric() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        HealthMetric metric = HealthMetric.builder()
                .id(1L)
                .patient(testPatient)
                .systolic(125)
                .diastolic(85)
                .heartRate(75)
                .spo2(96)
                .timestamp(now)
                .build();

        when(healthMetricRepository.findByPatientUserEmail("p@example.com"))
                .thenReturn(Arrays.asList(metric));

        // When
        Optional<HealthMetricDto> result = healthMetricService.getLatestMetric();

        // Then
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(125, result.get().getSystolic());
        assertEquals(85, result.get().getDiastolic());
        assertEquals(75, result.get().getHeartRate());
        assertEquals(96, result.get().getSpo2());
        assertEquals(now, result.get().getTimestamp());
    }

    @Test
    void getLatestMetric_MultipleMetrics() {
        // Given
        LocalDateTime baseTime = LocalDateTime.of(2023, 1, 1, 12, 0);
        
        HealthMetric metric1 = HealthMetric.builder()
                .id(1L)
                .patient(testPatient)
                .systolic(120)
                .diastolic(80)
                .heartRate(70)
                .spo2(98)
                .timestamp(baseTime) // Earliest
                .build();
        
        HealthMetric metric2 = HealthMetric.builder()
                .id(2L)
                .patient(testPatient)
                .systolic(130)
                .diastolic(85)
                .heartRate(75)
                .spo2(97)
                .timestamp(baseTime.plusHours(2)) // Latest
                .build();
        
        HealthMetric metric3 = HealthMetric.builder()
                .id(3L)
                .patient(testPatient)
                .systolic(125)
                .diastolic(82)
                .heartRate(72)
                .spo2(99)
                .timestamp(baseTime.plusHours(1)) // Middle
                .build();

        when(healthMetricRepository.findByPatientUserEmail("p@example.com"))
                .thenReturn(Arrays.asList(metric1, metric2, metric3));

        // When
        Optional<HealthMetricDto> result = healthMetricService.getLatestMetric();

        // Then
        assertNotNull(result);
        assertTrue(result.isPresent());
        // Should return the metric with the latest timestamp
        assertEquals(130, result.get().getSystolic());
        assertEquals(85, result.get().getDiastolic());
        assertEquals(75, result.get().getHeartRate());
        assertEquals(97, result.get().getSpo2());
        assertEquals(baseTime.plusHours(2), result.get().getTimestamp());
    }

    @Test
    void addMetric_PatientNotFound() {
        // Given
        HealthMetricDto dto = HealthMetricDto.builder()
                .systolic(120)
                .diastolic(80)
                .heartRate(70)
                .spo2(98)
                .timestamp(LocalDateTime.now())
                .build();

        when(patientRepository.findByUserEmail("p@example.com"))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            healthMetricService.addMetric(dto);
        });

        assertEquals("Patient not found", exception.getMessage());
        verify(healthMetricRepository, never()).save(any(HealthMetric.class));
    }

    @Test
    void addMetric_WithAllFields() {
        // Given
        LocalDateTime timestamp = LocalDateTime.of(2023, 12, 25, 14, 30);
        HealthMetricDto dto = HealthMetricDto.builder()
                .systolic(118)
                .diastolic(78)
                .heartRate(68)
                .spo2(97)
                .timestamp(timestamp)
                .build();

        when(patientRepository.findByUserEmail("p@example.com"))
                .thenReturn(Optional.of(testPatient));
        when(healthMetricRepository.save(any(HealthMetric.class)))
                .thenAnswer(i -> i.getArgument(0));

        // When
        HealthMetricDto result = healthMetricService.addMetric(dto);

        // Then
        assertNotNull(result);
        assertEquals(118, result.getSystolic());
        assertEquals(78, result.getDiastolic());
        assertEquals(68, result.getHeartRate());
        assertEquals(97, result.getSpo2());
        assertEquals(timestamp, result.getTimestamp());
        verify(healthMetricRepository).save(any(HealthMetric.class));
    }

    @Test
    void addMetric_WithMinimalValues() {
        // Given
        HealthMetricDto dto = HealthMetricDto.builder()
                .systolic(90)
                .diastolic(60)
                .heartRate(50)
                .spo2(90)
                .timestamp(LocalDateTime.now())
                .build();

        when(patientRepository.findByUserEmail("p@example.com"))
                .thenReturn(Optional.of(testPatient));
        when(healthMetricRepository.save(any(HealthMetric.class)))
                .thenAnswer(i -> i.getArgument(0));

        // When
        HealthMetricDto result = healthMetricService.addMetric(dto);

        // Then
        assertNotNull(result);
        assertEquals(90, result.getSystolic());
        assertEquals(60, result.getDiastolic());
        assertEquals(50, result.getHeartRate());
        assertEquals(90, result.getSpo2());
        verify(healthMetricRepository).save(any(HealthMetric.class));
    }

    @Test
    void addMetric_WithHighValues() {
        // Given
        HealthMetricDto dto = HealthMetricDto.builder()
                .systolic(180)
                .diastolic(120)
                .heartRate(120)
                .spo2(100)
                .timestamp(LocalDateTime.now())
                .build();

        when(patientRepository.findByUserEmail("p@example.com"))
                .thenReturn(Optional.of(testPatient));
        when(healthMetricRepository.save(any(HealthMetric.class)))
                .thenAnswer(i -> i.getArgument(0));

        // When
        HealthMetricDto result = healthMetricService.addMetric(dto);

        // Then
        assertNotNull(result);
        assertEquals(180, result.getSystolic());
        assertEquals(120, result.getDiastolic());
        assertEquals(120, result.getHeartRate());
        assertEquals(100, result.getSpo2());
        verify(healthMetricRepository).save(any(HealthMetric.class));
    }

    @Test
    void getMetricsForCurrentPatient_MultipleMetrics() {
        // Given
        LocalDateTime baseTime = LocalDateTime.of(2023, 1, 1, 12, 0);
        
        HealthMetric metric1 = HealthMetric.builder()
                .id(1L)
                .patient(testPatient)
                .systolic(120)
                .diastolic(80)
                .heartRate(70)
                .spo2(98)
                .timestamp(baseTime)
                .build();
        
        HealthMetric metric2 = HealthMetric.builder()
                .id(2L)
                .patient(testPatient)
                .systolic(125)
                .diastolic(82)
                .heartRate(72)
                .spo2(97)
                .timestamp(baseTime.plusHours(1))
                .build();
        
        HealthMetric metric3 = HealthMetric.builder()
                .id(3L)
                .patient(testPatient)
                .systolic(118)
                .diastolic(78)
                .heartRate(68)
                .spo2(99)
                .timestamp(baseTime.plusHours(2))
                .build();

        when(healthMetricRepository.findByPatientUserEmail("p@example.com"))
                .thenReturn(Arrays.asList(metric1, metric2, metric3));

        // When
        List<HealthMetricDto> result = healthMetricService.getMetricsForCurrentPatient();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        // Verify all metrics are returned
        assertEquals(120, result.get(0).getSystolic());
        assertEquals(125, result.get(1).getSystolic());
        assertEquals(118, result.get(2).getSystolic());
    }
}
