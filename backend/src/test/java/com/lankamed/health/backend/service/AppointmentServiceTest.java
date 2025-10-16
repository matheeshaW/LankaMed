package com.lankamed.health.backend.service;

import com.lankamed.health.backend.dto.AppointmentDto;
import com.lankamed.health.backend.model.*;
import com.lankamed.health.backend.repository.AppointmentRepository;
import com.lankamed.health.backend.model.patient.Patient;
import com.lankamed.health.backend.model.Appointment;
import com.lankamed.health.backend.model.ServiceCategory;
import org.junit.jupiter.api.AfterEach;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AppointmentService appointmentService;

    private User testUser;
    private Patient testPatient;
    private Hospital testHospital;
    private ServiceCategory testCategory;
    private StaffDetails testDoctor;
    private Appointment testAppointment;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .passwordHash("hashedPassword")
                .role(Role.PATIENT)
                .build();

        testPatient = Patient.builder()
                .patientId(1L)
                .user(testUser)
                .build();

        testHospital = Hospital.builder()
                .hospitalId(1L)
                .name("City General Hospital")
                .address("123 Hospital St")
                .build();

        testCategory = ServiceCategory.builder()
                .categoryId(1L)
                .name("Cardiology")
                .description("Heart and cardiovascular care")
                .build();

        User doctorUser = User.builder()
                .firstName("Dr. Jane")
                .lastName("Smith")
                .build();

        testDoctor = StaffDetails.builder()
                .staffId(2L)
                .user(doctorUser)
                .specialization("Cardiologist")
                .build();

        testAppointment = Appointment.builder()
                .appointmentId(1L)
                .patient(testPatient)
                .doctor(testDoctor)
                .hospital(testHospital)
                .serviceCategory(testCategory)
                .appointmentDateTime(LocalDateTime.of(2024, 1, 15, 10, 0))
                .status(Appointment.Status.SCHEDULED)
                .build();

        // Mock SecurityContext so AppointmentService.getCurrentUserEmail works
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        lenient().when(authentication.getName()).thenReturn("john.doe@example.com");
    }

    @AfterEach
    void tearDown() {
        // Clear SecurityContext to avoid test interference
        SecurityContextHolder.clearContext();
    }

    @Test
    void getPatientAppointments_Success() {
        // Given
        when(appointmentRepository.findByPatientUserEmailOrderByAppointmentDateTimeDesc("john.doe@example.com"))
                .thenReturn(Arrays.asList(testAppointment));

        // When
        List<AppointmentDto> result = appointmentService.getPatientAppointments();

        // Then
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Should return exactly one appointment");

        AppointmentDto dto = result.get(0);
        assertEquals(1L, dto.getAppointmentId());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), dto.getAppointmentDateTime());
        assertEquals(Appointment.Status.SCHEDULED, dto.getStatus());
        assertEquals("Dr. Jane Smith", dto.getDoctorName());
        assertEquals("Cardiologist", dto.getDoctorSpecialization());
        assertEquals("City General Hospital", dto.getHospitalName());
        assertEquals("Cardiology", dto.getServiceCategoryName());

        verify(appointmentRepository).findByPatientUserEmailOrderByAppointmentDateTimeDesc("john.doe@example.com");
    }

    @Test
    void getPatientAppointments_EmptyList() {
        // Given
        when(appointmentRepository.findByPatientUserEmailOrderByAppointmentDateTimeDesc("john.doe@example.com"))
                .thenReturn(Collections.emptyList());

        // When
        List<AppointmentDto> result = appointmentService.getPatientAppointments();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Result should be an empty list when there are no appointments");
    }

    @Test
    void getPatientAppointments_NullNestedData_ThrowsNpe() {
        // Edge case: repository returns an appointment with missing nested doctor/hospital data
        Appointment broken = Appointment.builder()
                .appointmentId(99L)
                .patient(testPatient)
                .doctor(null) // missing doctor
                .hospital(null)
                .serviceCategory(null)
                .appointmentDateTime(LocalDateTime.now())
                .status(Appointment.Status.SCHEDULED)
                .build();

        when(appointmentRepository.findByPatientUserEmailOrderByAppointmentDateTimeDesc("john.doe@example.com"))
                .thenReturn(Arrays.asList(broken));

        // Then
        assertThrows(NullPointerException.class, () -> appointmentService.getPatientAppointments());
    }

    @Test
    void getPatientAppointments_NoAuth_Throws() {
                // Simulate missing authentication in security context by clearing the context
                SecurityContextHolder.clearContext();

                // Then - getCurrentUserEmail should attempt to dereference a null Authentication and throw
                assertThrows(NullPointerException.class, () -> appointmentService.getPatientAppointments());
    }
}
