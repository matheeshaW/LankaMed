package com.lankamed.health.backend.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.lankamed.health.backend.repository.VisitRepository;
import com.lankamed.health.backend.service.interfaces.IReportDataProvider;

@Service("PATIENT_VISIT")
public class PatientVisitDataProvider implements IReportDataProvider {
    private final VisitRepository visitRepository;

    public PatientVisitDataProvider(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    @Override
    public Map<String, Object> fetchData(Map<String, Object> criteria) {
        // Example: Use criteria.get("from") / criteria.get("to") for date range
        // Simulated, in reality you'd use repository with JPQL/specification
        Map<String, Object> data = new HashMap<>();
        long totalVisits = visitRepository.count(); // Replace with date range count as needed
        data.put("totalVisits", totalVisits);
        data.put("uniquePatients", 10); // Replace with distinct patient logic
        data.put("reportPeriod", criteria.getOrDefault("from", "N/A") + " - " + criteria.getOrDefault("to", "N/A"));
        return data;
    }
}