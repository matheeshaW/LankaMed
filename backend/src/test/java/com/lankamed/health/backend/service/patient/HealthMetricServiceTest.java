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

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("p@example.com");
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
}
