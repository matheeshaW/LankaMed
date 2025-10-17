package com.lankamed.health.backend.controller;

import com.lankamed.health.backend.dto.SlotAvailabilityDto;
import com.lankamed.health.backend.service.DoctorSlotService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/slots")
@CrossOrigin(origins = "http://localhost:3000")
public class SlotsController {
    private final DoctorSlotService doctorSlotService;

    public SlotsController(DoctorSlotService doctorSlotService) {
        this.doctorSlotService = doctorSlotService;
    }

    @GetMapping("/{doctorId}")
    public ResponseEntity<SlotAvailabilityDto> availability(@PathVariable Long doctorId,
                                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(doctorSlotService.getAvailability(doctorId, date));
    }
}
