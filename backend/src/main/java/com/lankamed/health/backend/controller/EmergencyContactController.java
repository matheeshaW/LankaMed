package com.lankamed.health.backend.controller;

import com.lankamed.health.backend.dto.EmergencyContactDto;
import com.lankamed.health.backend.dto.CreateEmergencyContactDto;
import com.lankamed.health.backend.service.EmergencyContactService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients/me")
@CrossOrigin(origins = "http://localhost:3000")
public class EmergencyContactController {

    private final EmergencyContactService emergencyContactService;

    public EmergencyContactController(EmergencyContactService emergencyContactService) {
        this.emergencyContactService = emergencyContactService;
    }

    @GetMapping("/emergency-contacts")
    public ResponseEntity<List<EmergencyContactDto>> getEmergencyContacts() {
        return ResponseEntity.ok(emergencyContactService.getEmergencyContacts());
    }

    @PostMapping("/emergency-contacts")
    public ResponseEntity<EmergencyContactDto> createEmergencyContact(@Valid @RequestBody CreateEmergencyContactDto dto) {
        return ResponseEntity.ok(emergencyContactService.createEmergencyContact(dto));
    }

    @PutMapping("/emergency-contacts/{emergencyContactId}")
    public ResponseEntity<EmergencyContactDto> updateEmergencyContact(
            @PathVariable Long emergencyContactId,
            @Valid @RequestBody CreateEmergencyContactDto dto) {
        return ResponseEntity.ok(emergencyContactService.updateEmergencyContact(emergencyContactId, dto));
    }

    @DeleteMapping("/emergency-contacts/{emergencyContactId}")
    public ResponseEntity<Void> deleteEmergencyContact(@PathVariable Long emergencyContactId) {
        emergencyContactService.deleteEmergencyContact(emergencyContactId);
        return ResponseEntity.noContent().build();
    }
}


