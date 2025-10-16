package com.lankamed.health.backend.service;

import com.lankamed.health.backend.dto.CreateAppointmentDto;
import com.lankamed.health.backend.dto.UpdateAppointmentStatusDto;
import com.lankamed.health.backend.model.Appointment;
import com.lankamed.health.backend.model.Hospital;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceAdditionalTest {

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

	@BeforeEach
	void init() {
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
	}

	@Test
	@DisplayName("createAppointment - anonymous user falls back to default email and creates patient if missing")
	void createAppointment_anonymous_createsPatient() {
		when(authentication.getName()).thenReturn("anonymousUser");
		when(patientRepository.findByUserEmail("john.doe@example.com")).thenReturn(Optional.empty());
		User u = User.builder().email("john.doe@example.com").build();
		when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(u));
		when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

		Hospital hospital = Hospital.builder().hospitalId(1L).name("H").build();
		ServiceCategory sc = ServiceCategory.builder().categoryId(2L).name("C").build();
		StaffDetails doctor = StaffDetails.builder().staffId(3L).user(User.builder().firstName("A").lastName("B").build()).build();
		when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
		when(serviceCategoryRepository.findById(2L)).thenReturn(Optional.of(sc));
		when(staffDetailsRepository.findById(3L)).thenReturn(Optional.of(doctor));
		when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> {
			Appointment a = inv.getArgument(0);
			a.setAppointmentId(10L);
			return a;
		});

		CreateAppointmentDto dto = new CreateAppointmentDto();
		dto.setHospitalId(1L);
		dto.setServiceCategoryId(2L);
		dto.setDoctorId(3L);
		dto.setAppointmentDateTime(LocalDateTime.now().plusDays(1));

		var result = appointmentService.createAppointment(dto);
		assertEquals(10L, result.getAppointmentId());
	}

	@Test
	@DisplayName("createAppointment - priority=false results in PENDING status")
	void createAppointment_nonPriority_pending() {
		when(authentication.getName()).thenReturn("john@doe");
		Patient p = Patient.builder().user(User.builder().email("john@doe").build()).build();
		when(patientRepository.findByUserEmail("john@doe")).thenReturn(Optional.of(p));

		Hospital hospital = Hospital.builder().hospitalId(1L).name("H").build();
		ServiceCategory sc = ServiceCategory.builder().categoryId(2L).name("C").build();
		StaffDetails doctor = StaffDetails.builder().staffId(3L).user(User.builder().firstName("A").lastName("B").build()).build();
		when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
		when(serviceCategoryRepository.findById(2L)).thenReturn(Optional.of(sc));
		when(staffDetailsRepository.findById(3L)).thenReturn(Optional.of(doctor));
		when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

		CreateAppointmentDto dto = new CreateAppointmentDto();
		dto.setHospitalId(1L);
		dto.setServiceCategoryId(2L);
		dto.setDoctorId(3L);
		dto.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
		dto.setPriority(false);

		var result = appointmentService.createAppointment(dto);
		assertEquals(Appointment.Status.PENDING, result.getStatus());
	}

	@Test
	@DisplayName("createAppointment - doctor fallback: resolves by service category list when direct lookup missing")
	void createAppointment_doctorFallback_byCategory() {
		when(authentication.getName()).thenReturn("john@doe");
		Patient p = Patient.builder().user(User.builder().email("john@doe").build()).build();
		when(patientRepository.findByUserEmail("john@doe")).thenReturn(Optional.of(p));
		Hospital hospital = Hospital.builder().hospitalId(1L).name("H").build();
		ServiceCategory sc = ServiceCategory.builder().categoryId(2L).name("C").build();
		StaffDetails fallbackDoctor = StaffDetails.builder().staffId(9L).user(User.builder().firstName("F").lastName("D").build()).build();
		when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
		when(serviceCategoryRepository.findById(2L)).thenReturn(Optional.of(sc));
		when(staffDetailsRepository.findById(3L)).thenReturn(Optional.empty());
		when(staffDetailsRepository.findByServiceCategoryCategoryId(2L)).thenReturn(List.of(fallbackDoctor));
		when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

		CreateAppointmentDto dto = new CreateAppointmentDto();
		dto.setHospitalId(1L);
		dto.setServiceCategoryId(2L);
		dto.setDoctorId(3L);
		dto.setAppointmentDateTime(LocalDateTime.now().plusDays(1));

		var result = appointmentService.createAppointment(dto);
		assertEquals(9L, result.getDoctorId());
	}

	@Test
	@DisplayName("createAppointment - doctor fallback: resolves by hospital when category list empty")
	void createAppointment_doctorFallback_byHospital() {
		when(authentication.getName()).thenReturn("john@doe");
		Patient p = Patient.builder().user(User.builder().email("john@doe").build()).build();
		when(patientRepository.findByUserEmail("john@doe")).thenReturn(Optional.of(p));
		Hospital hospital = Hospital.builder().hospitalId(1L).name("H").build();
		ServiceCategory sc = ServiceCategory.builder().categoryId(2L).name("C").build();
		StaffDetails fallbackDoctor = StaffDetails.builder().staffId(8L).user(User.builder().firstName("H").lastName("D").build()).build();
		when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
		when(serviceCategoryRepository.findById(2L)).thenReturn(Optional.of(sc));
		when(staffDetailsRepository.findById(3L)).thenReturn(Optional.empty());
		when(staffDetailsRepository.findByServiceCategoryCategoryId(2L)).thenReturn(List.of());
		when(staffDetailsRepository.findByHospitalHospitalId(1L)).thenReturn(List.of(fallbackDoctor));
		when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

		CreateAppointmentDto dto = new CreateAppointmentDto();
		dto.setHospitalId(1L);
		dto.setServiceCategoryId(2L);
		dto.setDoctorId(3L);
		dto.setAppointmentDateTime(LocalDateTime.now().plusDays(1));

		var result = appointmentService.createAppointment(dto);
		assertEquals(8L, result.getDoctorId());
	}

	@Test
	@DisplayName("createAppointment - doctor fallback: resolves any doctor when hospital list empty")
	void createAppointment_doctorFallback_any() {
		when(authentication.getName()).thenReturn("john@doe");
		Patient p = Patient.builder().user(User.builder().email("john@doe").build()).build();
		when(patientRepository.findByUserEmail("john@doe")).thenReturn(Optional.of(p));
		Hospital hospital = Hospital.builder().hospitalId(1L).name("H").build();
		ServiceCategory sc = ServiceCategory.builder().categoryId(2L).name("C").build();
		StaffDetails anyDoctor = StaffDetails.builder().staffId(7L).user(User.builder().firstName("A").lastName("N").build()).build();
		when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
		when(serviceCategoryRepository.findById(2L)).thenReturn(Optional.of(sc));
		when(staffDetailsRepository.findById(3L)).thenReturn(Optional.empty());
		when(staffDetailsRepository.findByServiceCategoryCategoryId(2L)).thenReturn(List.of());
		when(staffDetailsRepository.findByHospitalHospitalId(1L)).thenReturn(List.of());
		when(staffDetailsRepository.findAll()).thenReturn(List.of(anyDoctor));
		when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

		CreateAppointmentDto dto = new CreateAppointmentDto();
		dto.setHospitalId(1L);
		dto.setServiceCategoryId(2L);
		dto.setDoctorId(3L);
		dto.setAppointmentDateTime(LocalDateTime.now().plusDays(1));

		var result = appointmentService.createAppointment(dto);
		assertEquals(7L, result.getDoctorId());
	}

	@Test
	@DisplayName("createAppointment - doctor fallback: creates placeholder when no doctors exist anywhere")
	void createAppointment_doctorFallback_placeholder() {
		when(authentication.getName()).thenReturn("john@doe");
		Patient p = Patient.builder().user(User.builder().email("john@doe").build()).build();
		when(patientRepository.findByUserEmail("john@doe")).thenReturn(Optional.of(p));
		Hospital hospital = Hospital.builder().hospitalId(1L).name("H").build();
		ServiceCategory sc = ServiceCategory.builder().categoryId(2L).name("C").build();
		when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
		when(serviceCategoryRepository.findById(2L)).thenReturn(Optional.of(sc));
		when(staffDetailsRepository.findById(3L)).thenReturn(Optional.empty());
		when(staffDetailsRepository.findByServiceCategoryCategoryId(2L)).thenReturn(List.of());
		when(staffDetailsRepository.findByHospitalHospitalId(1L)).thenReturn(List.of());
		when(staffDetailsRepository.findAll()).thenReturn(List.of());
		when(userRepository.findByEmail("placeholder.doctor@lankamed.com")).thenReturn(Optional.empty());
		when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
		when(staffDetailsRepository.save(any(StaffDetails.class))).thenAnswer(inv -> {
			StaffDetails sd = inv.getArgument(0);
			sd.setStaffId(99L);
			return sd;
		});
		when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

		CreateAppointmentDto dto = new CreateAppointmentDto();
		dto.setHospitalId(1L);
		dto.setServiceCategoryId(2L);
		dto.setDoctorId(3L);
		dto.setAppointmentDateTime(LocalDateTime.now().plusDays(1));

		var result = appointmentService.createAppointment(dto);
		assertEquals(99L, result.getDoctorId());
	}

	@Test
	@DisplayName("createAppointment - missing hospital and no fallback configured throws")
	void createAppointment_missingHospital_throws() {
		when(authentication.getName()).thenReturn("john@doe");
		Patient p = Patient.builder().user(User.builder().email("john@doe").build()).build();
		when(patientRepository.findByUserEmail("john@doe")).thenReturn(Optional.of(p));
		when(hospitalRepository.findById(1L)).thenReturn(Optional.empty());
		when(hospitalRepository.findAll()).thenReturn(List.of());

		CreateAppointmentDto dto = new CreateAppointmentDto();
		dto.setHospitalId(1L);
		dto.setServiceCategoryId(2L);
		dto.setDoctorId(3L);
		dto.setAppointmentDateTime(LocalDateTime.now().plusDays(1));

		RuntimeException ex = assertThrows(RuntimeException.class, () -> appointmentService.createAppointment(dto));
		assertTrue(ex.getMessage().contains("No hospitals configured"));
	}

	@Test
	@DisplayName("createAppointment - missing category and no fallback configured throws")
	void createAppointment_missingCategory_throws() {
		when(authentication.getName()).thenReturn("john@doe");
		Patient p = Patient.builder().user(User.builder().email("john@doe").build()).build();
		when(patientRepository.findByUserEmail("john@doe")).thenReturn(Optional.of(p));
		Hospital hospital = Hospital.builder().hospitalId(1L).name("H").build();
		when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
		when(serviceCategoryRepository.findById(2L)).thenReturn(Optional.empty());
		when(serviceCategoryRepository.findAll()).thenReturn(List.of());

		CreateAppointmentDto dto = new CreateAppointmentDto();
		dto.setHospitalId(1L);
		dto.setServiceCategoryId(2L);
		dto.setDoctorId(3L);
		dto.setAppointmentDateTime(LocalDateTime.now().plusDays(1));

		RuntimeException ex = assertThrows(RuntimeException.class, () -> appointmentService.createAppointment(dto));
		assertTrue(ex.getMessage().contains("No service categories configured"));
	}

	@Test
	@DisplayName("createAppointment - repository save failure surfaces as RuntimeException (error handling)")
	void createAppointment_dbFailure() {
		when(authentication.getName()).thenReturn("user@example.com");
		Patient p = Patient.builder().user(User.builder().email("user@example.com").build()).build();
		when(patientRepository.findByUserEmail("user@example.com")).thenReturn(Optional.of(p));
		Hospital hospital = Hospital.builder().hospitalId(1L).name("H").build();
		ServiceCategory sc = ServiceCategory.builder().categoryId(2L).name("C").build();
		StaffDetails doctor = StaffDetails.builder().staffId(3L).user(User.builder().firstName("A").lastName("B").build()).build();
		when(hospitalRepository.findById(1L)).thenReturn(Optional.of(hospital));
		when(serviceCategoryRepository.findById(2L)).thenReturn(Optional.of(sc));
		when(staffDetailsRepository.findById(3L)).thenReturn(Optional.of(doctor));
		when(appointmentRepository.save(any(Appointment.class))).thenThrow(new RuntimeException("DB down"));

		CreateAppointmentDto dto = new CreateAppointmentDto();
		dto.setHospitalId(1L);
		dto.setServiceCategoryId(2L);
		dto.setDoctorId(3L);
		dto.setAppointmentDateTime(LocalDateTime.now().plusDays(1));

		RuntimeException ex = assertThrows(RuntimeException.class, () -> appointmentService.createAppointment(dto));
		assertTrue(ex.getMessage().contains("DB down"));
	}

	@Test
	@DisplayName("getPatientAppointments - with auth uses repository by email; without uses findAll")
	void getPatientAppointments_authVsAnonymous() {
		when(authentication.getName()).thenReturn("john@doe");
		when(appointmentRepository.findByPatientUserEmailOrderByAppointmentDateTimeDesc("john@doe"))
				.thenReturn(List.of());
		var withAuth = appointmentService.getPatientAppointments();
		assertNotNull(withAuth);

		when(authentication.getName()).thenReturn(null);
		when(appointmentRepository.findAll()).thenReturn(List.of());
		var withoutAuth = appointmentService.getPatientAppointments();
		assertNotNull(withoutAuth);
	}
}


