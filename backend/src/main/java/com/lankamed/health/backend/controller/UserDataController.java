package com.lankamed.health.backend.controller;

import com.lankamed.health.backend.model.Appointment;
import com.lankamed.health.backend.repository.AppointmentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/user-data")
@CrossOrigin(origins = "http://localhost:3000")
public class UserDataController {

    private final AppointmentRepository appointmentRepository;

    public UserDataController(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }
    
    @GetMapping("/appointments")
    public ResponseEntity<Map<String, Object>> getAllAppointments() {
        List<Appointment> appointments = appointmentRepository.findAllWithDetails();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Appointment a : appointments) {
            Map<String, Object> m = new HashMap<>();
            m.put("appointmentId", a.getAppointmentId());
            m.put("appointmentDateTime", a.getAppointmentDateTime());
            m.put("status", a.getStatus());
            m.put("patientName", a.getPatient().getUser().getFirstName() + " " + a.getPatient().getUser().getLastName());
            m.put("hospitalName", a.getHospital().getName());
            m.put("serviceCategoryName", a.getServiceCategory().getName());
            m.put("doctorName", a.getDoctor().getUser().getFirstName() + " " + a.getDoctor().getUser().getLastName());
            m.put("doctorSpecialization", a.getDoctor().getSpecialization());
            m.put("priority", a.isPriority());
            list.add(m);
        }
        return ResponseEntity.ok(Map.of("success", true, "appointments", list));
    }
    
    @PutMapping("/appointments/{appointmentId}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable Long appointmentId,
                                                            @RequestBody Map<String, Object> body) {
        Optional<Appointment> opt = appointmentRepository.findByIdWithDetails(appointmentId);
        if (opt.isEmpty()) {
            return ResponseEntity.ok(Map.of("success", false, "error", "Appointment not found"));
        }
        Appointment a = opt.get();
        Object statusObj = body.get("status");
        if (statusObj == null) {
            return ResponseEntity.ok(Map.of("success", false, "error", "Missing status"));
        }
        String s = statusObj.toString().toUpperCase();
        try {
            a.setStatus(Appointment.Status.valueOf(s));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.ok(Map.of("success", false, "error", "Invalid status"));
        }
        Appointment saved = appointmentRepository.save(a);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "appointmentId", saved.getAppointmentId(),
                "status", saved.getStatus().name()
        ));
    }

    @PutMapping("/appointments/{appointmentId}")
    public ResponseEntity<Map<String, Object>> updateAppointment(@PathVariable Long appointmentId,
                                                                 @RequestBody Map<String, Object> body) {
        Optional<Appointment> opt = appointmentRepository.findByIdWithDetails(appointmentId);
        if (opt.isEmpty()) {
            return ResponseEntity.ok(Map.of("success", false, "error", "Appointment not found"));
        }
        Appointment a = opt.get();
        Object dtObj = body.get("appointmentDateTime");
        if (dtObj != null) {
            try {
                a.setAppointmentDateTime(java.time.LocalDateTime.parse(dtObj.toString()));
                        } catch (Exception e) {
                return ResponseEntity.ok(Map.of("success", false, "error", "Invalid appointmentDateTime"));
            }
        }
        Appointment saved = appointmentRepository.save(a);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "appointmentId", saved.getAppointmentId(),
                "appointmentDateTime", saved.getAppointmentDateTime()
        ));
    }

    @GetMapping("/doctors")
    public ResponseEntity<Map<String, Object>> getDoctors() {
        List<Map<String, Object>> doctorList = new ArrayList<>();

        Map<String, Object> d1 = new HashMap<>();
        d1.put("id", 1);
        d1.put("name", "Dr. Nimal Perera");
        d1.put("specialization", "Cardiology");
        d1.put("rating", 4.8);
        d1.put("fee", 1500);
        d1.put("hospitalId", 1);
        d1.put("hospital", "Colombo General Hospital");
        d1.put("hospitalName", "Colombo General Hospital");
        d1.put("hospitalAddress", "123 Main Street, Colombo 07");
        d1.put("hospitalContact", "+94 11 234 5678");
        d1.put("serviceCategoryId", 1);
        d1.put("serviceCategoryName", "Cardiology");
        d1.put("image", "👨‍⚕️");
        d1.put("reviewCount", 127);
        d1.put("experience", 15);
        doctorList.add(d1);

        Map<String, Object> d2 = new HashMap<>();
        d2.put("id", 2);
        d2.put("name", "Dr. S. Fernando");
        d2.put("specialization", "Dermatology");
        d2.put("rating", 4.5);
        d2.put("fee", 1200);
        d2.put("hospitalId", 1);
        d2.put("hospital", "Colombo General Hospital");
        d2.put("hospitalName", "Colombo General Hospital");
        d2.put("hospitalAddress", "123 Main Street, Colombo 07");
        d2.put("hospitalContact", "+94 11 234 5678");
        d2.put("serviceCategoryId", 2);
        d2.put("serviceCategoryName", "Dermatology");
        d2.put("image", "👩‍⚕️");
        d2.put("reviewCount", 89);
        d2.put("experience", 12);
        doctorList.add(d2);

        Map<String, Object> d3 = new HashMap<>();
        d3.put("id", 3);
        d3.put("name", "Dr. A. Wijesinghe");
        d3.put("specialization", "Pediatrics");
        d3.put("rating", 4.9);
        d3.put("fee", 1000);
        d3.put("hospitalId", 1);
        d3.put("hospital", "Colombo General Hospital");
        d3.put("hospitalName", "Colombo General Hospital");
        d3.put("hospitalAddress", "123 Main Street, Colombo 07");
        d3.put("hospitalContact", "+94 11 234 5678");
        d3.put("serviceCategoryId", 3);
        d3.put("serviceCategoryName", "Pediatrics");
        d3.put("image", "👨‍⚕️");
        d3.put("reviewCount", 156);
        d3.put("experience", 8);
        doctorList.add(d3);

        return ResponseEntity.ok(Map.of("success", true, "doctors", doctorList));
    }
}


