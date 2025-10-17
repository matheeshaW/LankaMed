package com.lankamed.health.backend.service;

import com.lankamed.health.backend.dto.CreateWaitlistDto;
import com.lankamed.health.backend.dto.WaitlistEntryDto;
import com.lankamed.health.backend.model.*;
import com.lankamed.health.backend.model.patient.Patient;
import com.lankamed.health.backend.repository.*;
import com.lankamed.health.backend.repository.patient.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WaitlistServiceTest {

    @Mock private WaitlistRepository waitlistRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private HospitalRepository hospitalRepository;
    @Mock private ServiceCategoryRepository serviceCategoryRepository;
    @Mock private StaffDetailsRepository staffDetailsRepository;
    @Mock private AppointmentRepository appointmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private Authentication authentication;
    @Mock private SecurityContext securityContext;

    private WaitlistServiceImpl waitlistService;

    private Patient patient;
    private Hospital hospital;
    private ServiceCategory category;
    private StaffDetails doctor;

    @BeforeEach
    void setUp() {
        // Set up mocks for security context
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        lenient().when(authentication.getName()).thenReturn("john.doe@example.com");

        // Set up test data
        User user = User.builder().userId(1L).email("john.doe@example.com").build();
        patient = Patient.builder().patientId(10L).user(user).build();
        hospital = Hospital.builder().hospitalId(1L).name("City General").build();
        category = ServiceCategory.builder().categoryId(2L).name("Cardiology").build();
        doctor = StaffDetails.builder()
                .staffId(3L)
                .user(User.builder().firstName("Dr. Jane").lastName("Smith").build())
                .specialization("Cardiology")
                .build();

        // Default mocks for common lookups - use lenient to avoid unnecessary stubbing errors
        lenient().when(patientRepository.findByUserEmail("john.doe@example.com")).thenReturn(Optional.of(patient));
        lenient().when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
        lenient().when(serviceCategoryRepository.findById(2L)).thenReturn(Optional.of(category));
        lenient().when(staffDetailsRepository.findById(3L)).thenReturn(Optional.of(doctor));
        
        // Mock findAll methods for fallback scenarios
        lenient().when(hospitalRepository.findAll()).thenReturn(List.of(hospital));
        lenient().when(serviceCategoryRepository.findAll()).thenReturn(List.of(category));
        lenient().when(staffDetailsRepository.findAll()).thenReturn(List.of(doctor));
    }

    // Constructor injection to override waitlistEnabled flag for specific tests
    private WaitlistServiceImpl getServiceWithFlag(boolean enabled) {
        return new WaitlistServiceImpl(
                waitlistRepository, patientRepository, hospitalRepository,
                serviceCategoryRepository, staffDetailsRepository,
                appointmentRepository, userRepository, enabled);
    }

    @Test
    @DisplayName("addToWaitlist - success with valid data creates entry")
    void addToWaitlist_success() {
        WaitlistServiceImpl service = getServiceWithFlag(true);
        when(waitlistRepository.save(any(WaitlistEntry.class))).thenAnswer(inv -> {
            WaitlistEntry e = inv.getArgument(0);
            e.setId(100L);
            return e;
        });

        CreateWaitlistDto dto = new CreateWaitlistDto();
        dto.setDoctorId(3L);
        dto.setHospitalId(1L);
        dto.setServiceCategoryId(2L);
        dto.setDesiredDateTime(LocalDateTime.now().plusDays(1));
        dto.setPriority(true);

        WaitlistEntryDto result = service.addToWaitlist(dto);
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(true, result.isPriority());
        assertEquals(WaitlistEntry.Status.QUEUED, result.getStatus());
    }

    @Test
    @DisplayName("addToWaitlist - feature disabled throws exception")
    void addToWaitlist_featureDisabled_throws() {
        WaitlistServiceImpl service = getServiceWithFlag(false);
        CreateWaitlistDto dto = new CreateWaitlistDto();
        dto.setDoctorId(3L);
        dto.setHospitalId(1L);
        dto.setServiceCategoryId(2L);
        dto.setDesiredDateTime(LocalDateTime.now().plusDays(1));

        UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class, () -> service.addToWaitlist(dto));
        assertEquals("Waitlist feature is disabled", ex.getMessage());
    }

    @Test
    @DisplayName("addToWaitlist - missing hospital throws exception")
    void addToWaitlist_missingHospital_throws() {
        WaitlistServiceImpl service = getServiceWithFlag(true);
        when(hospitalRepository.findById(1L)).thenReturn(Optional.empty());
        when(hospitalRepository.findAll()).thenReturn(List.of()); // No fallback hospitals

        CreateWaitlistDto dto = new CreateWaitlistDto();
        dto.setDoctorId(3L);
        dto.setHospitalId(1L);
        dto.setServiceCategoryId(2L);
        dto.setDesiredDateTime(LocalDateTime.now().plusDays(1));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.addToWaitlist(dto));
        assertTrue(ex.getMessage().contains("No hospitals configured"));
    }

    @Test
    @DisplayName("addToWaitlist - missing service category throws exception")
    void addToWaitlist_missingCategory_throws() {
        WaitlistServiceImpl service = getServiceWithFlag(true);
        when(serviceCategoryRepository.findById(2L)).thenReturn(Optional.empty());
        when(serviceCategoryRepository.findAll()).thenReturn(List.of()); // No fallback categories

        CreateWaitlistDto dto = new CreateWaitlistDto();
        dto.setDoctorId(3L);
        dto.setHospitalId(1L);
        dto.setServiceCategoryId(2L);
        dto.setDesiredDateTime(LocalDateTime.now().plusDays(1));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.addToWaitlist(dto));
        assertTrue(ex.getMessage().contains("No service categories configured"));
    }

    @Test
    @DisplayName("addToWaitlist - missing doctor throws exception")
    void addToWaitlist_missingDoctor_throws() {
        WaitlistServiceImpl service = getServiceWithFlag(true);
        when(staffDetailsRepository.findById(3L)).thenReturn(Optional.empty());
        when(staffDetailsRepository.findAll()).thenReturn(List.of()); // No fallback doctors

        CreateWaitlistDto dto = new CreateWaitlistDto();
        dto.setDoctorId(3L);
        dto.setHospitalId(1L);
        dto.setServiceCategoryId(2L);
        dto.setDesiredDateTime(LocalDateTime.now().plusDays(1));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.addToWaitlist(dto));
        assertTrue(ex.getMessage().contains("No doctors configured"));
    }

    @Test
    @DisplayName("getMyWaitlist - success returns list for authenticated user")
    void getMyWaitlist_success() {
        WaitlistServiceImpl service = getServiceWithFlag(true);
        WaitlistEntry entry = WaitlistEntry.builder()
                .id(1L)
                .patient(patient)
                .doctor(doctor)
                .hospital(hospital)
                .serviceCategory(category)
                .desiredDateTime(LocalDateTime.now().plusDays(2))
                .status(WaitlistEntry.Status.QUEUED)
                .build();
        when(waitlistRepository.findByPatientUserEmailAndStatusNotOrderByCreatedAtDesc("john.doe@example.com", WaitlistEntry.Status.PROMOTED))
                .thenReturn(List.of(entry));

        List<WaitlistEntryDto> result = service.getMyWaitlist();
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    @DisplayName("getMyWaitlist - feature disabled returns empty list")
    void getMyWaitlist_featureDisabled_empty() {
        WaitlistServiceImpl service = getServiceWithFlag(false);
        List<WaitlistEntryDto> result = service.getMyWaitlist();
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getMyWaitlist - anonymous user returns empty list")
    void getMyWaitlist_anonymous_empty() {
        WaitlistServiceImpl service = getServiceWithFlag(true);
        lenient().when(authentication.getName()).thenReturn("anonymousUser");
        List<WaitlistEntryDto> result = service.getMyWaitlist();
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("promoteToAppointment - success converts to appointment when slot available")
    void promoteToAppointment_success() {
        WaitlistServiceImpl service = getServiceWithFlag(true);
        WaitlistEntry entry = WaitlistEntry.builder()
                .id(5L)
                .patient(patient)
                .doctor(doctor)
                .hospital(hospital)
                .serviceCategory(category)
                .desiredDateTime(LocalDateTime.now().plusDays(1))
                .status(WaitlistEntry.Status.QUEUED)
                .priority(true)
                .build();
        when(waitlistRepository.findById(5L)).thenReturn(Optional.of(entry));
        when(appointmentRepository.findByDoctorStaffIdOrderByAppointmentDateTimeDesc(3L)).thenReturn(List.of());
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> {
            Appointment a = inv.getArgument(0);
            a.setAppointmentId(200L);
            return a;
        });
        when(waitlistRepository.save(any(WaitlistEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        WaitlistEntryDto result = service.promoteToAppointment(5L);
        assertEquals(WaitlistEntry.Status.PROMOTED, result.getStatus());
    }

    @Test
    @DisplayName("promoteToAppointment - feature disabled throws exception")
    void promoteToAppointment_featureDisabled_throws() {
        WaitlistServiceImpl service = getServiceWithFlag(false);
        UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class, () -> service.promoteToAppointment(1L));
        assertEquals("Waitlist feature is disabled", ex.getMessage());
    }

    @Test
    @DisplayName("promoteToAppointment - not found throws exception")
    void promoteToAppointment_notFound_throws() {
        WaitlistServiceImpl service = getServiceWithFlag(true);
        when(waitlistRepository.findById(404L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.promoteToAppointment(404L));
        assertTrue(ex.getMessage().contains("Waitlist entry not found"));
    }

    @Test
    @DisplayName("promoteToAppointment - already promoted throws exception")
    void promoteToAppointment_alreadyPromoted_throws() {
        WaitlistServiceImpl service = getServiceWithFlag(true);
        WaitlistEntry entry = WaitlistEntry.builder()
                .id(6L)
                .patient(patient)
                .doctor(doctor)
                .hospital(hospital)
                .serviceCategory(category)
                .desiredDateTime(LocalDateTime.now().plusDays(1))
                .status(WaitlistEntry.Status.PROMOTED)
                .build();
        when(waitlistRepository.findById(6L)).thenReturn(Optional.of(entry));
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.promoteToAppointment(6L));
        assertTrue(ex.getMessage().contains("Waitlist entry already processed"));
    }

    @Test
    @DisplayName("promoteToAppointment - slot conflict throws exception")
    void promoteToAppointment_slotConflict_throws() {
        WaitlistServiceImpl service = getServiceWithFlag(true);
        WaitlistEntry entry = WaitlistEntry.builder()
                .id(7L)
                .patient(patient)
                .doctor(doctor)
                .hospital(hospital)
                .serviceCategory(category)
                .desiredDateTime(LocalDateTime.now().plusDays(1))
                .status(WaitlistEntry.Status.QUEUED)
                .build();
        Appointment conflict = Appointment.builder()
                .appointmentId(99L)
                .patient(patient)
                .doctor(doctor)
                .hospital(hospital)
                .serviceCategory(category)
                .appointmentDateTime(entry.getDesiredDateTime())
                .build();
        when(waitlistRepository.findById(7L)).thenReturn(Optional.of(entry));
        when(appointmentRepository.findByDoctorStaffIdOrderByAppointmentDateTimeDesc(3L)).thenReturn(List.of(conflict));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.promoteToAppointment(7L));
        assertTrue(ex.getMessage().contains("No slot available"));
    }

    @Test
    @DisplayName("promoteToAppointment - DB failure during appointment save rolls back")
    void promoteToAppointment_dbFailure_rollback() {
        WaitlistServiceImpl service = getServiceWithFlag(true);
        WaitlistEntry entry = WaitlistEntry.builder()
                .id(8L)
                .patient(patient)
                .doctor(doctor)
                .hospital(hospital)
                .serviceCategory(category)
                .desiredDateTime(LocalDateTime.now().plusDays(1))
                .status(WaitlistEntry.Status.QUEUED)
                .build();
        when(waitlistRepository.findById(8L)).thenReturn(Optional.of(entry));
        when(appointmentRepository.findByDoctorStaffIdOrderByAppointmentDateTimeDesc(3L)).thenReturn(List.of());
        when(appointmentRepository.save(any(Appointment.class))).thenThrow(new RuntimeException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.promoteToAppointment(8L));
        assertEquals("DB error", ex.getMessage());
        // Verify status not updated (transaction rolled back)
        verify(waitlistRepository, never()).save(any(WaitlistEntry.class));
    }
}
