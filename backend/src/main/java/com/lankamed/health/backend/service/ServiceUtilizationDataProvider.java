package com.lankamed.health.backend.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.lankamed.health.backend.repository.VisitRepository;
import com.lankamed.health.backend.service.interfaces.IReportDataProvider;

@Service("SERVICE_UTILIZATION")
public class ServiceUtilizationDataProvider implements IReportDataProvider {
    private final VisitRepository visitRepository;

    public ServiceUtilizationDataProvider(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    @Override
    public Map<String, Object> fetchData(Map<String, Object> criteria) {
        // Simulate service utilization; ideally group by serviceCategory, real code would use repository with JPQL
        Map<String, Object> data = new HashMap<>();
        data.put("serviceCounts", new int[]{5, 12, 8}); // Replace with actual grouped data
        data.put("categoryNames", new String[]{"General", "Specialist", "Surgery"});
        return data;
    }
}