package com.lankamed.health.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lankamed.health.backend.model.Appointment;
import com.lankamed.health.backend.model.Hospital;
import com.lankamed.health.backend.model.ServiceCategory;
import com.lankamed.health.backend.model.StaffDetails;
import com.lankamed.health.backend.model.User;
import com.lankamed.health.backend.model.patient.Patient;
import com.lankamed.health.backend.repository.AppointmentRepository;
import com.lankamed.health.backend.repository.UserRepository;
import com.lankamed.health.backend.repository.patient.PatientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserDataController.class,
        excludeFilters = @ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.REGEX,
                                              pattern = "com\\.lankamed\\.health\\.backend\\.security\\..*"))
@AutoConfigureMockMvc(addFilters = false)
class UserDataControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private AppointmentRepository appointmentRepository;

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private PatientRepository patientRepository;

	private Appointment buildAppointment(Long id) {
		User user = User.builder().firstName("A").lastName("B").build();
		Patient patient = Patient.builder().user(user).build();
		StaffDetails doctor = StaffDetails.builder().user(User.builder().firstName("D").lastName("E").build()).specialization("Spec").build();
		Hospital hospital = Hospital.builder().name("Hosp").build();
		ServiceCategory sc = ServiceCategory.builder().name("Cat").build();
		return Appointment.builder()
				.appointmentId(id)
				.patient(patient)
				.doctor(doctor)
				.hospital(hospital)
				.serviceCategory(sc)
				.appointmentDateTime(LocalDateTime.now().plusDays(2))
				.status(Appointment.Status.PENDING)
				.priority(false)
				.build();
	}

	@Test
	@DisplayName("GET /api/user-data/appointments - returns flattened list for UI table")
	void getAllAppointments_success() throws Exception {
		Mockito.when(appointmentRepository.findAllWithDetails()).thenReturn(List.of(buildAppointment(1L)));
		mockMvc.perform(get("/api/user-data/appointments"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success", is(true)))
				.andExpect(jsonPath("$.appointments", hasSize(1)))
				.andExpect(jsonPath("$.appointments[0].appointmentId", is(1)));
	}

	@Test
	@DisplayName("PUT /api/user-data/appointments/{id}/status - missing status returns error json (negative)")
	void updateStatus_missingStatus() throws Exception {
		Mockito.when(appointmentRepository.findByIdWithDetails(5L)).thenReturn(Optional.of(buildAppointment(5L)));
		mockMvc.perform(put("/api/user-data/appointments/5/status")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success", is(false)))
				.andExpect(jsonPath("$.error", containsString("Missing status")));
	}

	@Test
	@DisplayName("PUT /api/user-data/appointments/{id}/status - appointment not found returns error json")
	void updateStatus_notFound() throws Exception {
		Mockito.when(appointmentRepository.findByIdWithDetails(404L)).thenReturn(Optional.empty());
		mockMvc.perform(put("/api/user-data/appointments/404/status")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\n  \"status\": \"APPROVED\"\n}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success", is(false)))
				.andExpect(jsonPath("$.error", containsString("Appointment not found")));
	}

	@Test
	@DisplayName("PUT /api/user-data/appointments/{id} - appointment not found returns error json")
	void updateAppointment_notFound() throws Exception {
		Mockito.when(appointmentRepository.findByIdWithDetails(404L)).thenReturn(Optional.empty());
		mockMvc.perform(put("/api/user-data/appointments/404")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\n  \"appointmentDateTime\": \"2026-01-01T12:00:00\"\n}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success", is(false)))
				.andExpect(jsonPath("$.error", containsString("Appointment not found")));
	}

	@Test
	@DisplayName("PUT /api/user-data/appointments/{id}/status - invalid enum returns error json (edge)")
	void updateStatus_invalidStatus() throws Exception {
		Mockito.when(appointmentRepository.findByIdWithDetails(6L)).thenReturn(Optional.of(buildAppointment(6L)));
		mockMvc.perform(put("/api/user-data/appointments/6/status")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\n  \"status\": \"NOT_A_STATUS\"\n}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success", is(false)))
				.andExpect(jsonPath("$.error", containsString("Invalid status")));
	}

	@Test
	@DisplayName("PUT /api/user-data/appointments/{id} - reschedules appointment date (reschedule logic)")
	void updateAppointment_reschedule_success() throws Exception {
		Mockito.when(appointmentRepository.findByIdWithDetails(7L)).thenReturn(Optional.of(buildAppointment(7L)));
		Mockito.when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));
		String newDate = LocalDateTime.now().plusDays(3).withNano(0).toString();
		mockMvc.perform(put("/api/user-data/appointments/7")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("appointmentDateTime", newDate))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success", is(true)))
				.andExpect(jsonPath("$.appointmentId", is(7)));
	}

	@Test
	@DisplayName("PUT /api/user-data/appointments/{id} - invalid date returns error json")
	void updateAppointment_reschedule_invalidDate() throws Exception {
		Mockito.when(appointmentRepository.findByIdWithDetails(8L)).thenReturn(Optional.of(buildAppointment(8L)));
		mockMvc.perform(put("/api/user-data/appointments/8")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\n  \"appointmentDateTime\": \"invalid\"\n}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success", is(false)))
				.andExpect(jsonPath("$.error", containsString("Invalid appointmentDateTime")));
	}
}


