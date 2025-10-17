package com.lankamed.health.backend.controller;

import com.lankamed.health.backend.dto.SlotAvailabilityDto;
import com.lankamed.health.backend.dto.WaitlistEntryDto;
import com.lankamed.health.backend.service.DoctorSlotService;
import com.lankamed.health.backend.service.WaitlistService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/waitlist")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminWaitlistController {
    private final WaitlistService waitlistService;
    private final DoctorSlotService doctorSlotService;
    private final boolean enabled;

    public AdminWaitlistController(WaitlistService waitlistService,
                                   DoctorSlotService doctorSlotService,
                                   @Value("${feature.waitlist.enabled:false}") boolean enabled) {
        this.waitlistService = waitlistService;
        this.doctorSlotService = doctorSlotService;
        this.enabled = enabled;
    }

    @PostMapping("/{id}/promote")
    public ResponseEntity<?> promote(@PathVariable Long id) {
        if (!enabled) return ResponseEntity.badRequest().body("Waitlist feature disabled");
        WaitlistEntryDto promoted = waitlistService.promoteToAppointment(id);
        return ResponseEntity.ok(promoted.getStatus().name());
    }

    @GetMapping("/availability/{doctorId}")
    public ResponseEntity<?> availability(@PathVariable Long doctorId,
                                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        SlotAvailabilityDto dto = doctorSlotService.getAvailability(doctorId, date);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/queue/{doctorId}")
    public ResponseEntity<?> queue(@PathVariable Long doctorId) {
        if (!enabled) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(waitlistService.listQueuedByDoctor(doctorId));
    }

    @GetMapping("/all")
    public ResponseEntity<?> all() {
        if (!enabled) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(waitlistService.getAllActiveWaitlistEntries());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateWaitlistStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        if (!enabled) return ResponseEntity.badRequest().body("Waitlist feature disabled");
        try {
            String newStatus = request.get("status");
            if (newStatus == null) {
                return ResponseEntity.badRequest().body("Status is required");
            }
            
            // Update waitlist entry status
            WaitlistEntryDto updated = waitlistService.updateWaitlistStatus(id, newStatus);
            return ResponseEntity.ok(Map.of("success", true, "status", updated.getStatus().name()));
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
