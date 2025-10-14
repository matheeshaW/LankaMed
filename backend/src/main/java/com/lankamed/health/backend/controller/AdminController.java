package com.lankamed.health.backend.controller;

import com.lankamed.health.backend.dto.AppointmentDto;
import com.lankamed.health.backend.dto.UpdateAppointmentStatusDto;
import com.lankamed.health.backend.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminController {

    private final AppointmentService appointmentService;

    public AdminController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentDto>> getAllAppointments() {
        List<AppointmentDto> appointments = appointmentService.getAllAppointments();
        return ResponseEntity.ok(appointments);
    }

    @PutMapping("/appointments/{appointmentId}/status")
    public ResponseEntity<AppointmentDto> updateAppointmentStatus(
            @PathVariable Long appointmentId,
            @Valid @RequestBody UpdateAppointmentStatusDto updateDto) {
        AppointmentDto appointment = appointmentService.updateAppointmentStatus(appointmentId, updateDto);
        return ResponseEntity.ok(appointment);
    }
}
