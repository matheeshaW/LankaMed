package com.lankamed.health.backend.service.patient;

import com.lankamed.health.backend.dto.patient.HealthMetricDto;
import com.lankamed.health.backend.model.patient.HealthMetric;
import com.lankamed.health.backend.model.patient.Patient;
import com.lankamed.health.backend.repository.patient.HealthMetricRepository;
import com.lankamed.health.backend.repository.patient.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HealthMetricService {
    @Autowired
    private HealthMetricRepository healthMetricRepository;
    @Autowired
    private PatientRepository patientRepository;

    public List<HealthMetricDto> getMetricsForCurrentPatient() {
        String email = getCurrentUserEmail();
        return healthMetricRepository.findByPatientUserEmail(email)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public Optional<HealthMetricDto> getLatestMetric() {
        String email = getCurrentUserEmail();
        return healthMetricRepository.findByPatientUserEmail(email)
            .stream()
            .max(Comparator.comparing(HealthMetric::getTimestamp))
            .map(this::toDto);
    }

    @Transactional
    public HealthMetricDto addMetric(HealthMetricDto dto) {
        String email = getCurrentUserEmail();
        Patient patient = patientRepository.findByUserEmail(email)
            .orElseThrow(() -> new RuntimeException("Patient not found"));
        HealthMetric metric = HealthMetric.builder()
            .patient(patient)
            .systolic(dto.getSystolic())
            .diastolic(dto.getDiastolic())
            .heartRate(dto.getHeartRate())
            .spo2(dto.getSpo2())
            .timestamp(dto.getTimestamp())
            .build();
        return toDto(healthMetricRepository.save(metric));
    }

    private HealthMetricDto toDto(HealthMetric m) {
        return HealthMetricDto.builder()
            .systolic(m.getSystolic())
            .diastolic(m.getDiastolic())
            .heartRate(m.getHeartRate())
            .spo2(m.getSpo2())
            .timestamp(m.getTimestamp())
            .build();
    }

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
