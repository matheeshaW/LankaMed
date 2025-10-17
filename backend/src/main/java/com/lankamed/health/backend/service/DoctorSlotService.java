package com.lankamed.health.backend.service;

import com.lankamed.health.backend.dto.SlotAvailabilityDto;

import java.time.LocalDate;

public interface DoctorSlotService {
    SlotAvailabilityDto getAvailability(Long doctorId, LocalDate date);

    boolean canBook(Long doctorId, LocalDate date);
}
