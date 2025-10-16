package com.lankamed.health.backend.service.patient;

import com.lankamed.health.backend.dto.patient.CreateEmergencyContactDto;
import com.lankamed.health.backend.dto.patient.EmergencyContactDto;
import com.lankamed.health.backend.model.patient.EmergencyContact;
import com.lankamed.health.backend.model.patient.Patient;
import com.lankamed.health.backend.model.User;
import com.lankamed.health.backend.repository.patient.EmergencyContactRepository;
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

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmergencyContactServiceTest {

    @Mock
    private EmergencyContactRepository emergencyContactRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private EmergencyContactService emergencyContactService;

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
    void getEmergencyContacts_ReturnsList() {
        EmergencyContact ec = EmergencyContact.builder().emergencyContactId(1L).fullName("Alice").patient(testPatient).build();
        when(emergencyContactRepository.findByPatientUserEmail("p@example.com")).thenReturn(Arrays.asList(ec));

        var res = emergencyContactService.getEmergencyContacts();
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals("Alice", res.get(0).getFullName());
    }

    @Test
    void createEmergencyContact_Success() {
        CreateEmergencyContactDto dto = new CreateEmergencyContactDto();
        dto.setFullName("Bob");
        dto.setPhone("123");

        when(patientRepository.findByUserEmail("p@example.com")).thenReturn(Optional.of(testPatient));
        when(emergencyContactRepository.save(any(EmergencyContact.class))).thenAnswer(i -> i.getArgument(0));

        EmergencyContactDto res = emergencyContactService.createEmergencyContact(dto);
        assertNotNull(res);
        assertEquals("Bob", res.getFullName());
        verify(emergencyContactRepository).save(any(EmergencyContact.class));
    }
}
