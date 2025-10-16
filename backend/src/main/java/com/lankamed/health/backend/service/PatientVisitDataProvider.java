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
     * Fetches filtered patient visit report data for the given criteria.
     * @param criteria a map of parameters (hospital, service, patientCategory, gender, minAge, maxAge, etc)
     * @return Map of report KPIs and data (never exposes unfiltered JPA entities)
     */
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
                LocalDateTime fromDate = fromObj instanceof LocalDateTime ?
                    (LocalDateTime) fromObj : LocalDateTime.parse(String.valueOf(fromObj));
                LocalDateTime toDate = toObj instanceof LocalDateTime ?
                    (LocalDateTime) toObj : LocalDateTime.parse(String.valueOf(toObj));
                predicates.add(cb.between(root.get("visitDate"), fromDate, toDate));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Map<String, Object> data = new HashMap<>();
        long totalVisits = visitRepository.count(spec);
        data.put("totalVisits", totalVisits);
        data.put("uniquePatients", -1); // TODO: count distinct root.get("patient")
        data.put("reportPeriod", criteria.getOrDefault("from", "N/A") + " - " + criteria.getOrDefault("to", "N/A"));
        return data;
    }
}