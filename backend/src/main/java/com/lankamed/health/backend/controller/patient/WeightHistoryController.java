package com.lankamed.health.backend.controller.patient;

import com.lankamed.health.backend.dto.patient.WeightRecordDto;
import com.lankamed.health.backend.service.patient.WeightRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients/me/weight-records")
@CrossOrigin(origins = "http://localhost:3000")
public class WeightHistoryController {
    @Autowired
    private WeightRecordService weightService;

    @GetMapping
    public ResponseEntity<List<WeightRecordDto>> getAllRecords() {
        return ResponseEntity.ok(weightService.getAllRecordsForCurrentPatient());
    }

    @PostMapping
    public ResponseEntity<WeightRecordDto> addRecord(@RequestBody WeightRecordDto dto) {
        return ResponseEntity.ok(weightService.addRecord(dto));
    }
}
