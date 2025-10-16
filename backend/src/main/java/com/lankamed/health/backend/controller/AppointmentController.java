package com.lankamed.health.backend.controller;

import com.lankamed.health.backend.dto.AppointmentDto;
import com.lankamed.health.backend.dto.CreateAppointmentDto;
import com.lankamed.health.backend.dto.UpdateAppointmentStatusDto;
import com.lankamed.health.backend.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/patients/me")
@CrossOrigin(origins = "http://localhost:3000")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentDto>> getAppointments() {
        List<AppointmentDto> appointments = appointmentService.getPatientAppointments();
        return ResponseEntity.ok(appointments);
    }

    @PostMapping("/appointments")
    public ResponseEntity<AppointmentDto> createAppointment(@Valid @RequestBody CreateAppointmentDto createAppointmentDto) {
        AppointmentDto appointment = appointmentService.createAppointment(createAppointmentDto);
        return ResponseEntity.ok(appointment);
    }
}
