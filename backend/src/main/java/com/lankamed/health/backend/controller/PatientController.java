package com.lankamed.health.backend.controller;

import com.lankamed.health.backend.dto.PatientProfileDto;
import com.lankamed.health.backend.dto.UpdatePatientProfileDto;
import com.lankamed.health.backend.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin(origins = "http://localhost:3000")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/me")
    public ResponseEntity<PatientProfileDto> getPatientProfile() {
        PatientProfileDto profile = patientService.getPatientProfile();
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me")
    public ResponseEntity<PatientProfileDto> updatePatientProfile(@Valid @RequestBody UpdatePatientProfileDto updateDto) {
        PatientProfileDto updatedProfile = patientService.updatePatientProfile(updateDto);
        return ResponseEntity.ok(updatedProfile);
    }
}
