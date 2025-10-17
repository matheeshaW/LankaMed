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
import java.util.Collections;
import java.util.List;
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

        // Setup security context for tests that need it
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        lenient().when(authentication.getName()).thenReturn("p@example.com");
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
        dto.setRelationship("Brother");
        dto.setEmail("bob@example.com");
        dto.setAddress("123 Main St");

        when(patientRepository.findByUserEmail("p@example.com")).thenReturn(Optional.of(testPatient));
        when(emergencyContactRepository.save(any(EmergencyContact.class))).thenAnswer(i -> i.getArgument(0));

        EmergencyContactDto res = emergencyContactService.createEmergencyContact(dto);
        assertNotNull(res);
        assertEquals("Bob", res.getFullName());
        assertEquals("123", res.getPhone());
        assertEquals("Brother", res.getRelationship());
        assertEquals("bob@example.com", res.getEmail());
        assertEquals("123 Main St", res.getAddress());
        verify(emergencyContactRepository).save(any(EmergencyContact.class));
    }

    @Test
    void createEmergencyContact_PatientNotFound() {
        CreateEmergencyContactDto dto = new CreateEmergencyContactDto();
        dto.setFullName("Bob");
        dto.setPhone("123");

        when(patientRepository.findByUserEmail("p@example.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            emergencyContactService.createEmergencyContact(dto);
        });
        assertEquals("Patient not found", exception.getMessage());
        verify(emergencyContactRepository, never()).save(any(EmergencyContact.class));
    }

    @Test
    void updateEmergencyContact_Success() {
        Long emergencyContactId = 1L;
        CreateEmergencyContactDto dto = new CreateEmergencyContactDto();
        dto.setFullName("Updated Bob");
        dto.setPhone("456");
        dto.setRelationship("Father");
        dto.setEmail("updated@example.com");
        dto.setAddress("456 Oak St");

        EmergencyContact existingContact = EmergencyContact.builder()
                .emergencyContactId(emergencyContactId)
                .fullName("Bob")
                .phone("123")
                .relationship("Brother")
                .email("bob@example.com")
                .address("123 Main St")
                .patient(testPatient)
                .build();

        when(emergencyContactRepository.findById(emergencyContactId)).thenReturn(Optional.of(existingContact));
        when(emergencyContactRepository.save(any(EmergencyContact.class))).thenAnswer(i -> i.getArgument(0));

        EmergencyContactDto res = emergencyContactService.updateEmergencyContact(emergencyContactId, dto);
        assertNotNull(res);
        assertEquals("Updated Bob", res.getFullName());
        assertEquals("456", res.getPhone());
        assertEquals("Father", res.getRelationship());
        assertEquals("updated@example.com", res.getEmail());
        assertEquals("456 Oak St", res.getAddress());
        verify(emergencyContactRepository).save(existingContact);
    }

    @Test
    void updateEmergencyContact_NotFound() {
        Long emergencyContactId = 999L;
        CreateEmergencyContactDto dto = new CreateEmergencyContactDto();
        dto.setFullName("Updated Bob");

        when(emergencyContactRepository.findById(emergencyContactId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            emergencyContactService.updateEmergencyContact(emergencyContactId, dto);
        });
        assertEquals("Emergency contact not found", exception.getMessage());
        verify(emergencyContactRepository, never()).save(any(EmergencyContact.class));
    }

    @Test
    void updateEmergencyContact_UnauthorizedAccess() {
        Long emergencyContactId = 1L;
        CreateEmergencyContactDto dto = new CreateEmergencyContactDto();
        dto.setFullName("Updated Bob");

        // Create a different patient
        User differentUser = User.builder().userId(2L).email("different@example.com").build();
        Patient differentPatient = Patient.builder().patientId(2L).user(differentUser).build();

        EmergencyContact existingContact = EmergencyContact.builder()
                .emergencyContactId(emergencyContactId)
                .fullName("Bob")
                .patient(differentPatient)
                .build();

        when(emergencyContactRepository.findById(emergencyContactId)).thenReturn(Optional.of(existingContact));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            emergencyContactService.updateEmergencyContact(emergencyContactId, dto);
        });
        assertEquals("Unauthorized access to emergency contact", exception.getMessage());
        verify(emergencyContactRepository, never()).save(any(EmergencyContact.class));
    }

    @Test
    void deleteEmergencyContact_Success() {
        Long emergencyContactId = 1L;
        EmergencyContact existingContact = EmergencyContact.builder()
                .emergencyContactId(emergencyContactId)
                .fullName("Bob")
                .patient(testPatient)
                .build();

        when(emergencyContactRepository.findById(emergencyContactId)).thenReturn(Optional.of(existingContact));

        assertDoesNotThrow(() -> {
            emergencyContactService.deleteEmergencyContact(emergencyContactId);
        });
        verify(emergencyContactRepository).delete(existingContact);
    }

    @Test
    void deleteEmergencyContact_NotFound() {
        Long emergencyContactId = 999L;

        when(emergencyContactRepository.findById(emergencyContactId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            emergencyContactService.deleteEmergencyContact(emergencyContactId);
        });
        assertEquals("Emergency contact not found", exception.getMessage());
        verify(emergencyContactRepository, never()).delete(any(EmergencyContact.class));
    }

    @Test
    void deleteEmergencyContact_UnauthorizedAccess() {
        Long emergencyContactId = 1L;

        // Create a different patient
        User differentUser = User.builder().userId(2L).email("different@example.com").build();
        Patient differentPatient = Patient.builder().patientId(2L).user(differentUser).build();

        EmergencyContact existingContact = EmergencyContact.builder()
                .emergencyContactId(emergencyContactId)
                .fullName("Bob")
                .patient(differentPatient)
                .build();

        when(emergencyContactRepository.findById(emergencyContactId)).thenReturn(Optional.of(existingContact));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            emergencyContactService.deleteEmergencyContact(emergencyContactId);
        });
        assertEquals("Unauthorized access to emergency contact", exception.getMessage());
        verify(emergencyContactRepository, never()).delete(any(EmergencyContact.class));
    }

    @Test
    void getEmergencyContacts_EmptyList() {
        when(emergencyContactRepository.findByPatientUserEmail("p@example.com")).thenReturn(Collections.emptyList());

        List<EmergencyContactDto> res = emergencyContactService.getEmergencyContacts();
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    void getEmergencyContacts_MultipleContacts() {
        EmergencyContact ec1 = EmergencyContact.builder()
                .emergencyContactId(1L)
                .fullName("Alice")
                .phone("111")
                .relationship("Mother")
                .patient(testPatient)
                .build();
        EmergencyContact ec2 = EmergencyContact.builder()
                .emergencyContactId(2L)
                .fullName("Bob")
                .phone("222")
                .relationship("Father")
                .patient(testPatient)
                .build();

        when(emergencyContactRepository.findByPatientUserEmail("p@example.com"))
                .thenReturn(Arrays.asList(ec1, ec2));

        List<EmergencyContactDto> res = emergencyContactService.getEmergencyContacts();
        assertNotNull(res);
        assertEquals(2, res.size());
        assertEquals("Alice", res.get(0).getFullName());
        assertEquals("Bob", res.get(1).getFullName());
    }

    @Test
    void createEmergencyContact_WithAllFields() {
        CreateEmergencyContactDto dto = new CreateEmergencyContactDto();
        dto.setFullName("John Doe");
        dto.setRelationship("Spouse");
        dto.setPhone("555-1234");
        dto.setEmail("john@example.com");
        dto.setAddress("123 Main Street, City, State");

        when(patientRepository.findByUserEmail("p@example.com")).thenReturn(Optional.of(testPatient));
        when(emergencyContactRepository.save(any(EmergencyContact.class))).thenAnswer(i -> i.getArgument(0));

        EmergencyContactDto res = emergencyContactService.createEmergencyContact(dto);
        assertNotNull(res);
        assertEquals("John Doe", res.getFullName());
        assertEquals("Spouse", res.getRelationship());
        assertEquals("555-1234", res.getPhone());
        assertEquals("john@example.com", res.getEmail());
        assertEquals("123 Main Street, City, State", res.getAddress());
    }

    @Test
    void updateEmergencyContact_WithAllFields() {
        Long emergencyContactId = 1L;
        CreateEmergencyContactDto dto = new CreateEmergencyContactDto();
        dto.setFullName("Jane Doe");
        dto.setRelationship("Sister");
        dto.setPhone("555-5678");
        dto.setEmail("jane@example.com");
        dto.setAddress("456 Oak Avenue, Town, State");

        EmergencyContact existingContact = EmergencyContact.builder()
                .emergencyContactId(emergencyContactId)
                .fullName("Old Name")
                .phone("111-1111")
                .relationship("Old Relationship")
                .email("old@example.com")
                .address("Old Address")
                .patient(testPatient)
                .build();

        when(emergencyContactRepository.findById(emergencyContactId)).thenReturn(Optional.of(existingContact));
        when(emergencyContactRepository.save(any(EmergencyContact.class))).thenAnswer(i -> i.getArgument(0));

        EmergencyContactDto res = emergencyContactService.updateEmergencyContact(emergencyContactId, dto);
        assertNotNull(res);
        assertEquals("Jane Doe", res.getFullName());
        assertEquals("Sister", res.getRelationship());
        assertEquals("555-5678", res.getPhone());
        assertEquals("jane@example.com", res.getEmail());
        assertEquals("456 Oak Avenue, Town, State", res.getAddress());
    }
}
