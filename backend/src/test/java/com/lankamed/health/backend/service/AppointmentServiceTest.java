package com.lankamed.health.backend.service;

import com.lankamed.health.backend.dto.AppointmentDto;
import com.lankamed.health.backend.model.*;
import com.lankamed.health.backend.repository.AppointmentRepository;
import com.lankamed.health.backend.repository.PatientRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

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
                .status(Appointment.Status.PENDING)
                .build();

        // Mock SecurityContext
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("john.doe@example.com");
    }

    @Test
    void getPatientAppointments_Success() {
        // Given
        when(patientRepository.findByUserEmail("john.doe@example.com"))
                .thenReturn(Optional.of(testPatient));
        when(appointmentRepository.findByPatientUserEmailOrderByAppointmentDateTimeDesc("john.doe@example.com"))
                .thenReturn(Arrays.asList(testAppointment));

        // When
        List<AppointmentDto> result = appointmentService.getPatientAppointments();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        
        AppointmentDto appointmentDto = result.get(0);
        assertEquals(1L, appointmentDto.getAppointmentId());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), appointmentDto.getAppointmentDateTime());
        assertEquals(Appointment.Status.PENDING, appointmentDto.getStatus());
        assertEquals("Dr. Jane Smith", appointmentDto.getDoctorName());
        assertEquals("Cardiologist", appointmentDto.getDoctorSpecialization());
        assertEquals("City General Hospital", appointmentDto.getHospitalName());
        assertEquals("Cardiology", appointmentDto.getServiceCategoryName());

        verify(appointmentRepository).findByPatientUserEmailOrderByAppointmentDateTimeDesc("john.doe@example.com");
    }

    @Test
    void getPatientAppointments_EmptyList() {
        // Given
        when(patientRepository.findByUserEmail("john.doe@example.com"))
                .thenReturn(Optional.of(testPatient));
        when(appointmentRepository.findByPatientUserEmailOrderByAppointmentDateTimeDesc("john.doe@example.com"))
                .thenReturn(Arrays.asList());

        // When
        List<AppointmentDto> result = appointmentService.getPatientAppointments();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(appointmentRepository).findByPatientUserEmailOrderByAppointmentDateTimeDesc("john.doe@example.com");
    }

    @Test
    void getPatientAppointments_MultipleAppointments() {
        // Given
        Appointment pastAppointment = Appointment.builder()
                .appointmentId(2L)
                .patient(testPatient)
                .doctor(testDoctor)
                .hospital(testHospital)
                .serviceCategory(testCategory)
                .appointmentDateTime(LocalDateTime.of(2023, 12, 1, 14, 0))
                .status(Appointment.Status.COMPLETED)
                .build();

        Appointment futureAppointment = Appointment.builder()
                .appointmentId(3L)
                .patient(testPatient)
                .doctor(testDoctor)
                .hospital(testHospital)
                .serviceCategory(testCategory)
                .appointmentDateTime(LocalDateTime.of(2024, 2, 1, 9, 0))
                .status(Appointment.Status.PENDING)
                .build();

        List<Appointment> appointments = Arrays.asList(futureAppointment, testAppointment, pastAppointment);

        when(patientRepository.findByUserEmail("john.doe@example.com"))
                .thenReturn(Optional.of(testPatient));
        when(appointmentRepository.findByPatientUserEmailOrderByAppointmentDateTimeDesc("john.doe@example.com"))
                .thenReturn(appointments);

        // When
        List<AppointmentDto> result = appointmentService.getPatientAppointments();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        
        // Verify appointments are ordered by date descending (newest first)
        assertEquals(3L, result.get(0).getAppointmentId()); // Future appointment
        assertEquals(1L, result.get(1).getAppointmentId()); // Current appointment
        assertEquals(2L, result.get(2).getAppointmentId()); // Past appointment

        verify(appointmentRepository).findByPatientUserEmailOrderByAppointmentDateTimeDesc("john.doe@example.com");
    }
}
