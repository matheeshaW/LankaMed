package com.lankamed.health.backend.service;

import com.lankamed.health.backend.dto.SlotAvailabilityDto;
import com.lankamed.health.backend.model.StaffDetails;
import com.lankamed.health.backend.repository.AppointmentRepository;
import com.lankamed.health.backend.repository.StaffDetailsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class DoctorSlotServiceImpl implements DoctorSlotService {
    private final AppointmentRepository appointmentRepository;
    private final StaffDetailsRepository staffDetailsRepository;
    private final boolean enabled;
    private final int capacity;

    public DoctorSlotServiceImpl(AppointmentRepository appointmentRepository,
                                 StaffDetailsRepository staffDetailsRepository,
                                 @Value("${feature.slots.enabled:false}") boolean enabled,
                                 @Value("${feature.slots.capacity:10}") int capacity) {
        this.appointmentRepository = appointmentRepository;
        this.staffDetailsRepository = staffDetailsRepository;
        this.enabled = enabled;
        this.capacity = capacity;
    }

    @Override
    public SlotAvailabilityDto getAvailability(Long doctorId, LocalDate date) {
        if (!enabled) {
            return SlotAvailabilityDto.builder()
                    .doctorId(doctorId)
                    .date(date.toString())
                    .capacity(0)
                    .booked(0)
                    .available(0)
                    .build();
        }
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);
        long booked = appointmentRepository.countByDoctorStaffIdAndAppointmentDateTimeBetween(doctorId, start, end);
        StaffDetails doctor = staffDetailsRepository.findById(doctorId).orElse(null);
        String name = doctor != null && doctor.getUser() != null ? doctor.getUser().getFirstName() + " " + doctor.getUser().getLastName() : null;
        int available = Math.max(0, capacity - (int) booked);
        return SlotAvailabilityDto.builder()
                .doctorId(doctorId)
                .doctorName(name)
                .date(date.toString())
                .capacity(capacity)
                .booked((int) booked)
                .available(available)
                .build();
    }

    @Override
    public boolean canBook(Long doctorId, LocalDate date) {
        return getAvailability(doctorId, date).getAvailable() > 0;
    }
}
