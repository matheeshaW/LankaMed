package com.lankamed.health.backend.controller;

import com.lankamed.health.backend.dto.AppointmentDto;
import com.lankamed.health.backend.dto.UpdateAppointmentStatusDto;
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

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                com.lankamed.health.backend.config.SecurityConfig.class,
                com.lankamed.health.backend.security.JwtAuthenticationFilter.class
        }))
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AppointmentService appointmentService;

	@Test
	@DisplayName("GET /api/admin/appointments - returns list for admin view")
	void getAllAppointments_success() throws Exception {
		AppointmentDto a1 = AppointmentDto.builder().appointmentId(11L).status(Appointment.Status.PENDING).build();
		Mockito.when(appointmentService.getAllAppointments()).thenReturn(List.of(a1));

		mockMvc.perform(get("/api/admin/appointments"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$[0].appointmentId", is(11)));
	}

	@Test
	@DisplayName("PUT /api/admin/appointments/{id}/status - updates status and returns updated dto")
	void updateStatus_success() throws Exception {
		AppointmentDto updated = AppointmentDto.builder().appointmentId(9L).status(Appointment.Status.APPROVED).build();
		Mockito.when(appointmentService.updateAppointmentStatus(eq(9L), any(UpdateAppointmentStatusDto.class)))
				.thenReturn(updated);

		String body = "{\n  \"status\": \"APPROVED\"\n}";
		mockMvc.perform(put("/api/admin/appointments/9/status")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status", is("APPROVED")));
	}
}


