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

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("p@example.com");
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
}
