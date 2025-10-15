package com.lankamed.health.backend.controller.patient;

import com.lankamed.health.backend.dto.patient.BloodPressureRecordDto;
import com.lankamed.health.backend.service.patient.BloodPressureRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients/me/blood-pressure-records")
@CrossOrigin(origins = "http://localhost:3000")
public class BloodPressureTrendController {
    @Autowired
    private BloodPressureRecordService bpService;

    @GetMapping
    public ResponseEntity<List<BloodPressureRecordDto>> getAllRecords() {
        return ResponseEntity.ok(bpService.getAllRecordsForCurrentPatient());
    }

    @PostMapping
    public ResponseEntity<BloodPressureRecordDto> addRecord(@RequestBody BloodPressureRecordDto dto) {
        return ResponseEntity.ok(bpService.addRecord(dto));
    }
}
