package com.lankamed.health.backend.dto;

import com.lankamed.health.backend.model.Appointment;
import com.lankamed.health.backend.model.Hospital;
import com.lankamed.health.backend.model.ServiceCategory;
import com.lankamed.health.backend.model.StaffDetails;
import com.lankamed.health.backend.model.User;
import com.lankamed.health.backend.model.patient.Patient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentDtoTest {

	@Test
	@DisplayName("fromAppointment maps nested fields correctly (doctor/hospital/category)")
	void fromAppointment_mapsCorrectly() {
		User doctorUser = User.builder().firstName("Dr").lastName("Who").build();
		StaffDetails doctor = StaffDetails.builder().staffId(9L).user(doctorUser).specialization("Time travel").build();
		Hospital hospital = Hospital.builder().name("TARDIS Clinic").build();
		ServiceCategory sc = ServiceCategory.builder().name("Chronology").build();
		Patient patient = Patient.builder().user(User.builder().firstName("P").lastName("T").build()).build();
		Appointment a = Appointment.builder()
				.appointmentId(42L)
				.patient(patient)
				.doctor(doctor)
				.hospital(hospital)
				.serviceCategory(sc)
				.appointmentDateTime(LocalDateTime.now().plusDays(1))
				.status(Appointment.Status.PENDING)
				.priority(true)
				.build();

		AppointmentDto dto = AppointmentDto.fromAppointment(a);
		assertEquals(42L, dto.getAppointmentId());
		assertEquals("Dr Who", dto.getDoctorName());
		assertEquals("Time travel", dto.getDoctorSpecialization());
		assertEquals("TARDIS Clinic", dto.getHospitalName());
		assertEquals("Chronology", dto.getServiceCategoryName());
		assertEquals(9L, dto.getDoctorId());
		assertTrue(dto.isPriority());
	}
}


