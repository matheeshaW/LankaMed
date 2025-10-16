package com.lankamed.health.backend.service.patient;

import com.lankamed.health.backend.dto.patient.BloodPressureRecordDto;
import com.lankamed.health.backend.model.patient.BloodPressureRecord;
import com.lankamed.health.backend.model.patient.Patient;
import com.lankamed.health.backend.repository.patient.BloodPressureRecordRepository;
import com.lankamed.health.backend.repository.patient.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BloodPressureRecordService {
    @Autowired
    private BloodPressureRecordRepository bpRepository;
    @Autowired
    private PatientRepository patientRepository;

    public List<BloodPressureRecordDto> getAllRecordsForCurrentPatient() {
        String email = getCurrentUserEmail();
        return bpRepository.findByPatientUserEmail(email)
            .stream()
            .sorted(Comparator.comparing(BloodPressureRecord::getTimestamp))
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public BloodPressureRecordDto addRecord(BloodPressureRecordDto dto) {
        String email = getCurrentUserEmail();
        Patient patient = patientRepository.findByUserEmail(email)
            .orElseThrow(() -> new RuntimeException("Patient not found"));
        BloodPressureRecord record = BloodPressureRecord.builder()
            .patient(patient)
            .systolic(dto.getSystolic())
            .diastolic(dto.getDiastolic())
            .timestamp(dto.getTimestamp())
            .build();
        return toDto(bpRepository.save(record));
    }

    private BloodPressureRecordDto toDto(BloodPressureRecord record) {
        return BloodPressureRecordDto.builder()
            .systolic(record.getSystolic())
            .diastolic(record.getDiastolic())
            .timestamp(record.getTimestamp())
            .build();
    }

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
