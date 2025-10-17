package com.lankamed.health.backend.controller.patient;

import com.lankamed.health.backend.dto.patient.AllergyDto;
import com.lankamed.health.backend.dto.patient.CreateAllergyDto;
import com.lankamed.health.backend.dto.patient.CreateMedicalConditionDto;
import com.lankamed.health.backend.dto.patient.MedicalConditionDto;
import com.lankamed.health.backend.dto.patient.PrescriptionDto;
import com.lankamed.health.backend.service.patient.MedicalHistoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients/me")
@CrossOrigin(origins = "http://localhost:3000")
public class MedicalHistoryController {

    private final MedicalHistoryService medicalHistoryService;

    public MedicalHistoryController(MedicalHistoryService medicalHistoryService) {
        this.medicalHistoryService = medicalHistoryService;
    }

    @GetMapping("/conditions")
    public ResponseEntity<List<MedicalConditionDto>> getMedicalConditions() {
        List<MedicalConditionDto> conditions = medicalHistoryService.getMedicalConditions();
        return ResponseEntity.ok(conditions);
    }

    @PostMapping("/conditions")
    public ResponseEntity<MedicalConditionDto> createMedicalCondition(@Valid @RequestBody CreateMedicalConditionDto createDto) {
        MedicalConditionDto condition = medicalHistoryService.createMedicalCondition(createDto);
        return ResponseEntity.ok(condition);
    }

    @PutMapping("/conditions/{conditionId}")
    public ResponseEntity<MedicalConditionDto> updateMedicalCondition(
            @PathVariable Long conditionId,
            @Valid @RequestBody CreateMedicalConditionDto updateDto) {
        MedicalConditionDto condition = medicalHistoryService.updateMedicalCondition(conditionId, updateDto);
        return ResponseEntity.ok(condition);
    }

    @DeleteMapping("/conditions/{conditionId}")
    public ResponseEntity<Void> deleteMedicalCondition(@PathVariable Long conditionId) {
        medicalHistoryService.deleteMedicalCondition(conditionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/allergies")
    public ResponseEntity<List<AllergyDto>> getAllergies() {
        List<AllergyDto> allergies = medicalHistoryService.getAllergies();
        return ResponseEntity.ok(allergies);
    }

    @PostMapping("/allergies")
    public ResponseEntity<AllergyDto> createAllergy(@Valid @RequestBody CreateAllergyDto createDto) {
        AllergyDto allergy = medicalHistoryService.createAllergy(createDto);
        return ResponseEntity.ok(allergy);
    }

    @PutMapping("/allergies/{allergyId}")
    public ResponseEntity<AllergyDto> updateAllergy(
            @PathVariable Long allergyId,
            @Valid @RequestBody CreateAllergyDto updateDto) {
        AllergyDto allergy = medicalHistoryService.updateAllergy(allergyId, updateDto);
        return ResponseEntity.ok(allergy);
    }

    @DeleteMapping("/allergies/{allergyId}")
    public ResponseEntity<Void> deleteAllergy(@PathVariable Long allergyId) {
        medicalHistoryService.deleteAllergy(allergyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/prescriptions")
    public ResponseEntity<List<PrescriptionDto>> getPrescriptions() {
        List<PrescriptionDto> prescriptions = medicalHistoryService.getPrescriptions();
        return ResponseEntity.ok(prescriptions);
    }

}


