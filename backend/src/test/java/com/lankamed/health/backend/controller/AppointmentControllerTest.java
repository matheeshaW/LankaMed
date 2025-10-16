package com.lankamed.health.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lankamed.health.backend.dto.AppointmentDto;
import com.lankamed.health.backend.dto.CreateAppointmentDto;
import com.lankamed.health.backend.model.Appointment;
import com.lankamed.health.backend.service.AppointmentService;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AppointmentController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                com.lankamed.health.backend.config.SecurityConfig.class,
                com.lankamed.health.backend.security.JwtAuthenticationFilter.class
        }))
@AutoConfigureMockMvc(addFilters = false)
class AppointmentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private AppointmentService appointmentService;

	@Test
	@DisplayName("POST /api/patients/me/appointments - creates appointment successfully with valid payload")
	void createAppointment_success() throws Exception {
		CreateAppointmentDto dto = new CreateAppointmentDto();
		dto.setDoctorId(3L);
		dto.setHospitalId(1L);
		dto.setServiceCategoryId(2L);
		dto.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
		dto.setPriority(true);

		AppointmentDto response = AppointmentDto.builder()
				.appointmentId(100L)
				.status(Appointment.Status.CONFIRMED)
				.priority(true)
				.build();

		Mockito.when(appointmentService.createAppointment(any(CreateAppointmentDto.class)))
				.thenReturn(response);

		mockMvc.perform(post("/api/patients/me/appointments")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.appointmentId", is(100)))
				.andExpect(jsonPath("$.status", is("CONFIRMED")))
				.andExpect(jsonPath("$.priority", is(true)));
	}

	@Test
	@DisplayName("POST /api/patients/me/appointments - returns 400 when required fields are missing")
	void createAppointment_validationError_missingFields() throws Exception {
		// Missing appointmentDateTime and others triggers @Valid failures
		String invalidJson = "{\n" +
				"  \"doctorId\": 3,\n" +
				"  \"hospitalId\": 1\n" +
				"}";

		mockMvc.perform(post("/api/patients/me/appointments")
				.contentType(MediaType.APPLICATION_JSON)
				.content(invalidJson))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("GET /api/patients/me/appointments - returns user's appointments list")
	void getAppointments_success() throws Exception {
		AppointmentDto a1 = AppointmentDto.builder().appointmentId(1L).status(Appointment.Status.PENDING).build();
		AppointmentDto a2 = AppointmentDto.builder().appointmentId(2L).status(Appointment.Status.CONFIRMED).build();
		Mockito.when(appointmentService.getPatientAppointments()).thenReturn(List.of(a1, a2));

		mockMvc.perform(get("/api/patients/me/appointments"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].appointmentId", is(1)))
				.andExpect(jsonPath("$[1].status", is("CONFIRMED")));
	}
}


