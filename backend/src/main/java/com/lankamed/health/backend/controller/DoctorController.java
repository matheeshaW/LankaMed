package com.lankamed.health.backend.controller;

import com.lankamed.health.backend.dto.DoctorDto;
import com.lankamed.health.backend.service.DoctorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@CrossOrigin(origins = "http://localhost:3000")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public ResponseEntity<List<DoctorDto>> searchDoctors(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String specialization) {
        List<DoctorDto> doctors = doctorService.searchDoctors(name, specialization);
        return ResponseEntity.ok(doctors);
    }
}
