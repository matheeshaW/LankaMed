package com.lankamed.health.backend.service;

import com.lankamed.health.backend.dto.CreateAppointmentDto;
import com.lankamed.health.backend.dto.UpdateAppointmentStatusDto;
import com.lankamed.health.backend.model.Appointment;
import com.lankamed.health.backend.model.Hospital;
import com.lankamed.health.backend.model.Role;
import com.lankamed.health.backend.model.ServiceCategory;
import com.lankamed.health.backend.model.StaffDetails;
import com.lankamed.health.backend.model.User;
import com.lankamed.health.backend.model.patient.Patient;
import com.lankamed.health.backend.repository.AppointmentRepository;
import com.lankamed.health.backend.repository.HospitalRepository;
import com.lankamed.health.backend.repository.ServiceCategoryRepository;
import com.lankamed.health.backend.repository.StaffDetailsRepository;
import com.lankamed.health.backend.repository.UserRepository;
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
class AppointmentServiceCreateAndUpdateTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private HospitalRepository hospitalRepository;
    @Mock private ServiceCategoryRepository serviceCategoryRepository;
    @Mock private StaffDetailsRepository staffDetailsRepository;
    @Mock private UserRepository userRepository;
    @Mock private Authentication authentication;
    @Mock private SecurityContext securityContext;

    @InjectMocks
    private AppointmentService appointmentService;

    private Patient patient;
    private Hospital hospital;
    private ServiceCategory category;
    private StaffDetails doctor;

    @BeforeEach
    void setUp() {
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        lenient().when(authentication.getName()).thenReturn("john.doe@example.com");

        User user = User.builder().userId(1L).email("john.doe@example.com").role(Role.PATIENT).build();
        patient = Patient.builder().patientId(10L).user(user).build();
        hospital = Hospital.builder().hospitalId(1L).name("City General").build();
        category = ServiceCategory.builder().categoryId(2L).name("Cardiology").build();
        doctor = StaffDetails.builder()
                .staffId(3L)
                .user(User.builder().firstName("Dr. Jane").lastName("Smith").build())
                .specialization("Cardiology")
                .build();
    }

    @Test
    void createAppointment_success_prioritySetsConfirmed() {
        CreateAppointmentDto dto = new CreateAppointmentDto();
        dto.setHospitalId(1L);
        dto.setServiceCategoryId(2L);
        dto.setDoctorId(3L);
        dto.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        dto.setPriority(true);

        when(patientRepository.findByUserEmail("john.doe@example.com")).thenReturn(Optional.of(patient));
        when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
        when(serviceCategoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(staffDetailsRepository.findById(3L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> {
            Appointment a = inv.getArgument(0);
            a.setAppointmentId(123L);
            return a;
        });

        var result = appointmentService.createAppointment(dto);
        assertNotNull(result);
        assertEquals(123L, result.getAppointmentId());
        assertEquals(Appointment.Status.CONFIRMED, result.getStatus());
    }

    @Test
    void updateAppointmentStatus_success() {
        Appointment existing = Appointment.builder()
                .appointmentId(50L)
                .patient(patient)
                .doctor(doctor)
                .hospital(hospital)
                .serviceCategory(category)
                .appointmentDateTime(LocalDateTime.now().plusDays(2))
                .status(Appointment.Status.PENDING)
                .build();

        when(appointmentRepository.findById(50L)).thenReturn(Optional.of(existing));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateAppointmentStatusDto updateDto = new UpdateAppointmentStatusDto();
        updateDto.setStatus(Appointment.Status.CANCELLED);

        var updated = appointmentService.updateAppointmentStatus(50L, updateDto);
        assertEquals(Appointment.Status.CANCELLED, updated.getStatus());
    }

    @Test
    void updateAppointmentStatus_notFound_throws() {
        when(appointmentRepository.findById(404L)).thenReturn(Optional.empty());

        UpdateAppointmentStatusDto updateDto = new UpdateAppointmentStatusDto();
        updateDto.setStatus(Appointment.Status.CONFIRMED);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                appointmentService.updateAppointmentStatus(404L, updateDto));
        assertTrue(ex.getMessage().contains("Appointment not found"));
    }
}


