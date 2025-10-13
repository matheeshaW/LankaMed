package com.lankamed.health.backend.controller;

import com.lankamed.health.backend.dto.AppointmentDto;
import com.lankamed.health.backend.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
