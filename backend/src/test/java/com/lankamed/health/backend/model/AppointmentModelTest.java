package com.lankamed.health.backend.model;

import com.lankamed.health.backend.model.patient.Patient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentModelTest {

	@Test
	@DisplayName("prePersist sets createdAt when null, status defaults to PENDING, priority false")
	void prePersist_defaults() {
		Appointment a = Appointment.builder()
				.patient(Patient.builder().build())
				.doctor(StaffDetails.builder().build())
				.hospital(Hospital.builder().build())
				.serviceCategory(ServiceCategory.builder().build())
				.appointmentDateTime(LocalDateTime.now().plusDays(1))
				.build();
		assertNull(a.getCreatedAt());
		assertEquals(Appointment.Status.PENDING, a.getStatus());
		assertFalse(a.isPriority());
		a.prePersist();
		assertNotNull(a.getCreatedAt());
		assertTrue(a.getCreatedAt().isBefore(Instant.now().plusSeconds(2)));
	}
}


