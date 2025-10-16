package com.lankamed.health.backend.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lankamed.health.backend.model.Visit;
import com.lankamed.health.backend.repository.VisitRepository;
import com.lankamed.health.backend.service.interfaces.IReportDataProvider;

import jakarta.persistence.criteria.Predicate;

/**
 * Provides filtered data for report generation based on user-selected criteria.
 * <p>
 * <b>SOLID Compliance:</b>
 * <ul>
 *   <li><b>Single Responsibility:</b> Encapsulates all data querying/report filter logic for patient visit metrics.</li>
 *   <li><b>Open/Closed:</b> Can be extended with new filter fields without modifying existing logic structure.</li>
 * </ul>
 * <b>Design:</b>
 * <ul>
 *   <li>Annotated with @Service and @Transactional for Spring-managed business logic.</li>
 *   <li>Does not expose JPA entities directly; acts as middle layer for conversion to report DTOs/views.</li>
 *   <li>Meant for use in a report generation strategy/factory pattern setup as part of extensible report service.</li>
 * </ul>
 */
@Service("PATIENT_VISIT")
@Transactional
public class PatientVisitDataProvider implements IReportDataProvider {
    private final VisitRepository visitRepository;

    public PatientVisitDataProvider(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    /**
     * Fetches filtered patient visit report data using JPA Specifications for dynamic querying
     * @param criteria filter parameters (hospital, service, patientCategory, gender, age range, date range)
     * @return Map containing KPIs: totalVisits, uniquePatients, reportPeriod
     */
    @Override
    public Map<String, Object> fetchData(Map<String, Object> criteria) {
        // Build dynamic query using JPA Specifications
        Specification<Visit> spec = (root, query, cb) -> {
            java.util.List<Predicate> predicates = new java.util.ArrayList<>();
            
            // Apply filters based on criteria
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
            
            // Handle date range filtering with proper time boundaries
            if (criteria.get("from") != null && criteria.get("to") != null) {
                Object fromObj = criteria.get("from");
                Object toObj = criteria.get("to");
                LocalDateTime fromDate, toDate;
                
                // Parse date strings and set appropriate time boundaries
                if (fromObj instanceof LocalDateTime fromDateTime) {
                    fromDate = fromDateTime;
                } else {
                    String fromStr = String.valueOf(fromObj);
                    fromDate = fromStr.contains("T") 
                        ? LocalDateTime.parse(fromStr)
                        : java.time.LocalDate.parse(fromStr).atStartOfDay();
                }
                
                if (toObj instanceof LocalDateTime toDateTime) {
                    toDate = toDateTime;
                } else {
                    String toStr = String.valueOf(toObj);
                    toDate = toStr.contains("T")
                        ? LocalDateTime.parse(toStr)
                        : java.time.LocalDate.parse(toStr).atTime(23, 59, 59);
                }
                
                predicates.add(cb.between(root.get("visitDate"), fromDate, toDate));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        
        // Calculate KPIs
        Map<String, Object> data = new HashMap<>();
        long totalVisits = visitRepository.count(spec);
        data.put("totalVisits", totalVisits);
        
        // Count unique patients (Note: This could be optimized with a custom query)
        long uniquePatients = visitRepository.findAll(spec).stream()
            .map(visit -> visit.getPatient().getPatientId())
            .distinct()
            .count();
        data.put("uniquePatients", uniquePatients);
        
        data.put("reportPeriod", criteria.getOrDefault("from", "N/A") + " - " + criteria.getOrDefault("to", "N/A"));
        return data;
    }
}