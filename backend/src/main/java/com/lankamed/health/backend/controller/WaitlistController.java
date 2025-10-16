package com.lankamed.health.backend.controller;

import com.lankamed.health.backend.dto.CreateWaitlistDto;
import com.lankamed.health.backend.dto.WaitlistEntryDto;
import com.lankamed.health.backend.service.WaitlistService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients/me")
@CrossOrigin(origins = "http://localhost:3000")
public class WaitlistController {
    private final WaitlistService waitlistService;
    private final boolean enabled;

    public WaitlistController(
            WaitlistService waitlistService,
            @Value("${feature.waitlist.enabled:false}") boolean enabled) {
        this.waitlistService = waitlistService;
        this.enabled = enabled;
    }

    @PostMapping("/waitlist")
    public ResponseEntity<?> add(@Valid @RequestBody CreateWaitlistDto dto) {
        if (!enabled) return ResponseEntity.badRequest().body("Waitlist feature disabled");
        WaitlistEntryDto entry = waitlistService.addToWaitlist(dto);
        return ResponseEntity.ok(entry.getId());
    }

    @GetMapping("/waitlist")
    public ResponseEntity<?> mine() {
        if (!enabled) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(waitlistService.getMyWaitlist());
    }
}
