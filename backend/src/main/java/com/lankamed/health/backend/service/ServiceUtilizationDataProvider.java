package com.lankamed.health.backend.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.lankamed.health.backend.model.Visit;
import com.lankamed.health.backend.repository.VisitRepository;
import com.lankamed.health.backend.service.interfaces.IReportDataProvider;

import jakarta.persistence.criteria.Predicate;

@Service("SERVICE_UTILIZATION")
public class ServiceUtilizationDataProvider implements IReportDataProvider {
    private final VisitRepository visitRepository;

    public ServiceUtilizationDataProvider(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    @Override
    public Map<String, Object> fetchData(Map<String, Object> criteria) {
        Specification<Visit> spec = (root, query, cb) -> {
            java.util.List<Predicate> predicates = new java.util.ArrayList<>();
            if (criteria.get("hospitalId") != null)
                predicates.add(cb.equal(root.get("hospitalId"), criteria.get("hospitalId")));
            if (criteria.get("serviceCategory") != null)
                predicates.add(cb.equal(root.get("serviceCategory"), criteria.get("serviceCategory")));
            if (criteria.get("patientCategory") != null)
                predicates.add(cb.equal(root.get("patientCategory"), criteria.get("patientCategory")));
            if (criteria.get("gender") != null)
                predicates.add(cb.equal(root.get("gender"), criteria.get("gender")));
            if (criteria.get("minAge") != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("age"), (Integer) criteria.get("minAge")));
            if (criteria.get("maxAge") != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("age"), (Integer) criteria.get("maxAge")));
            if (criteria.get("from") != null && criteria.get("to") != null) {
                Object fromObj = criteria.get("from");
                Object toObj = criteria.get("to");
                java.time.LocalDateTime fromDate;
                java.time.LocalDateTime toDate;
                
                if (fromObj instanceof java.time.LocalDateTime) {
                    fromDate = (java.time.LocalDateTime) fromObj;
                } else {
                    // Parse date-only string (YYYY-MM-DD) and set time to start of day
                    String fromStr = String.valueOf(fromObj);
                    fromDate = fromStr.contains("T") 
                        ? java.time.LocalDateTime.parse(fromStr)
                        : java.time.LocalDate.parse(fromStr).atStartOfDay();
                }
                
                if (toObj instanceof java.time.LocalDateTime) {
                    toDate = (java.time.LocalDateTime) toObj;
                } else {
                    // Parse date-only string (YYYY-MM-DD) and set time to end of day
                    String toStr = String.valueOf(toObj);
                    toDate = toStr.contains("T")
                        ? java.time.LocalDateTime.parse(toStr)
                        : java.time.LocalDate.parse(toStr).atTime(23, 59, 59);
                }
                
                predicates.add(cb.between(root.get("visitDate"), fromDate, toDate));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        // Placeholder group-by logic by serviceCategory (ideally: use custom repo or projection)
        var allVisits = visitRepository.findAll(spec);
        Map<String, Integer> grouped = new HashMap<>();
        for (Visit v : allVisits) {
            String category = v.getServiceCategory() != null ? v.getServiceCategory() : "Unknown";
            grouped.put(category, grouped.getOrDefault(category, 0) + 1);
        }
        String[] categoryNames = grouped.keySet().toArray(new String[0]);
        int[] serviceCounts = grouped.values().stream().mapToInt(Integer::intValue).toArray();
        Map<String, Object> data = new HashMap<>();
        data.put("serviceCounts", serviceCounts);
        data.put("categoryNames", categoryNames);
        // TODO: Replace with DB-side group-by for large datasets
        return data;
    }
}