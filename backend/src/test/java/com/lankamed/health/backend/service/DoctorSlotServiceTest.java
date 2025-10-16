package com.lankamed.health.backend.service;

import com.lankamed.health.backend.dto.SlotAvailabilityDto;
import com.lankamed.health.backend.model.StaffDetails;
import com.lankamed.health.backend.model.User;
import com.lankamed.health.backend.repository.AppointmentRepository;
import com.lankamed.health.backend.repository.StaffDetailsRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DoctorSlotServiceTest {

    @Test
    void availability_flagOff_returnsZeros() {
        AppointmentRepository apptRepo = mock(AppointmentRepository.class);
        StaffDetailsRepository staffRepo = mock(StaffDetailsRepository.class);
        DoctorSlotServiceImpl service = new DoctorSlotServiceImpl(apptRepo, staffRepo, false, 10);
        SlotAvailabilityDto dto = service.getAvailability(1L, LocalDate.now());
        assertEquals(0, dto.getCapacity());
        assertEquals(0, dto.getAvailable());
        assertFalse(service.canBook(1L, LocalDate.now()));
    }

    @Test
    void availability_flagOn_countsBookings() {
        AppointmentRepository apptRepo = mock(AppointmentRepository.class);
        StaffDetailsRepository staffRepo = mock(StaffDetailsRepository.class);
        when(apptRepo.countByDoctorStaffIdAndAppointmentDateTimeBetween(anyLong(), any(), any())).thenReturn(7L);
        when(staffRepo.findById(1L)).thenReturn(java.util.Optional.of(
                StaffDetails.builder().staffId(1L).user(User.builder().firstName("Dr").lastName("Who").build()).build()
        ));
        DoctorSlotServiceImpl service = new DoctorSlotServiceImpl(apptRepo, staffRepo, true, 10);
        SlotAvailabilityDto dto = service.getAvailability(1L, LocalDate.now());
        assertEquals(10, dto.getCapacity());
        assertEquals(7, dto.getBooked());
        assertEquals(3, dto.getAvailable());
        assertTrue(service.canBook(1L, LocalDate.now()));
    }
}
