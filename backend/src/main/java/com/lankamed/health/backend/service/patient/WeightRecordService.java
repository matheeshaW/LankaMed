package com.lankamed.health.backend.service.patient;

import com.lankamed.health.backend.dto.patient.WeightRecordDto;
import com.lankamed.health.backend.model.patient.WeightRecord;
import com.lankamed.health.backend.model.patient.Patient;
import com.lankamed.health.backend.repository.patient.WeightRecordRepository;
import com.lankamed.health.backend.repository.patient.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WeightRecordService {
    @Autowired
    private WeightRecordRepository weightRepository;
    @Autowired
    private PatientRepository patientRepository;

    public List<WeightRecordDto> getAllRecordsForCurrentPatient() {
        String email = getCurrentUserEmail();
        return weightRepository.findByPatientUserEmail(email)
            .stream()
            .sorted(Comparator.comparing(WeightRecord::getTimestamp))
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public WeightRecordDto addRecord(WeightRecordDto dto) {
        String email = getCurrentUserEmail();
        Patient patient = patientRepository.findByUserEmail(email)
            .orElseThrow(() -> new RuntimeException("Patient not found"));
        WeightRecord record = WeightRecord.builder()
            .patient(patient)
            .weightKg(dto.getWeightKg())
            .timestamp(dto.getTimestamp())
            .build();
        return toDto(weightRepository.save(record));
    }

    private WeightRecordDto toDto(WeightRecord record) {
        return WeightRecordDto.builder()
            .weightKg(record.getWeightKg())
            .timestamp(record.getTimestamp())
            .build();
    }

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
