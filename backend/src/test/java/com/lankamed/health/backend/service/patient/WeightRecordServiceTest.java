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

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("p@example.com");
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

}
