package com.lankamed.health.backend.controller;

import com.lankamed.health.backend.dto.DoctorDto;
import com.lankamed.health.backend.dto.DoctorProfileDto;
import com.lankamed.health.backend.service.DoctorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        System.out.println("DoctorController: Received request with name=" + name + ", specialization=" + specialization);
        List<DoctorDto> doctors = doctorService.searchDoctors(name, specialization);
        System.out.println("DoctorController: Returning " + doctors.size() + " doctors");
        return ResponseEntity.ok(doctors);
    }
    
    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debugDoctors() {
        Map<String, Object> debug = new HashMap<>();
        try {
            List<DoctorDto> doctors = doctorService.searchDoctors(null, null);
            List<DoctorProfileDto> profiles = doctorService.searchDoctorProfiles(null, null);
            debug.put("totalDoctors", doctors.size());
            debug.put("totalProfiles", profiles.size());
            debug.put("doctors", doctors);
            debug.put("profiles", profiles);
            debug.put("message", "Debug endpoint working");
        } catch (Exception e) {
            debug.put("error", e.getMessage());
            debug.put("message", "Error occurred");
            e.printStackTrace();
        }
        return ResponseEntity.ok(debug);
    }
    
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Doctor endpoint is working");
        response.put("timestamp", System.currentTimeMillis());
        response.put("backendStatus", "Running");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/data-check")
    public ResponseEntity<Map<String, Object>> checkData() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<DoctorDto> doctors = doctorService.searchDoctors(null, null);
            response.put("doctorsCount", doctors.size());
            response.put("doctors", doctors);
            response.put("status", "Data available");
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("status", "Error");
        }
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/create-test-data")
    public ResponseEntity<Map<String, Object>> createTestData() {
        Map<String, Object> response = new HashMap<>();
        try {
            // This is a simple way to create test data without the full DataInitializer
            // In a real app, you'd use a proper service
            response.put("message", "Test data creation endpoint - use DataInitializer instead");
            response.put("status", "Use DataInitializer for data creation");
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("status", "Error");
        }
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/profiles")
    public ResponseEntity<List<DoctorProfileDto>> searchDoctorProfiles(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String specialization) {
        System.out.println("DoctorController: Received profile request with name=" + name + ", specialization=" + specialization);
        List<DoctorProfileDto> profiles = doctorService.searchDoctorProfiles(name, specialization);
        System.out.println("DoctorController: Returning " + profiles.size() + " doctor profiles");
        return ResponseEntity.ok(profiles);
    }
}
