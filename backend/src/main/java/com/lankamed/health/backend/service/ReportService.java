package com.lankamed.health.backend.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lankamed.health.backend.model.ReportAudit;
import com.lankamed.health.backend.model.User;
import com.lankamed.health.backend.repository.ReportAuditRepository;
import com.lankamed.health.backend.repository.UserRepository;
import com.lankamed.health.backend.service.interfaces.IPdfExporter;
import com.lankamed.health.backend.service.interfaces.IReportDataProvider;
import com.lankamed.health.backend.service.interfaces.IReportGenerator;

//Single Responsibility Principle
@Service
public class ReportService {

    private final Map<String, IReportDataProvider> providerMap;
    private final IReportGenerator reportGenerator;
    private final IPdfExporter pdfExporter;
    private final ReportAuditRepository auditRepository;
    private final UserRepository userRepository;

    //Dependency Inversion Principle
    public ReportService(Map<String, IReportDataProvider> providerMap,
                         IReportGenerator reportGenerator,
                         IPdfExporter pdfExporter,
                         ReportAuditRepository auditRepository,
                         UserRepository userRepository) {
        this.providerMap = providerMap;
        this.reportGenerator = reportGenerator;
        this.pdfExporter = pdfExporter;
        this.auditRepository = auditRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReportResponse createReport(String reportType,
                                       Map<String, Object> criteria,
                                       Map<String, Object> filters,
                                       String email) {  // <-- renamed from username
        ReportAudit audit = new ReportAudit();
        audit.setReportType(reportType);
        audit.setCriteriaJson(criteria != null ? criteria.toString() : "{}");
        audit.setGeneratedOn(LocalDateTime.now());
        audit.setSuccess(false);
        audit.setNotes("");

        // ✅ Load user by email (matches your entity)
        User user = userRepository.findByEmail(email).orElse(null);
        audit.setUser(user);

        // Populate audit filter fields from criteria
        if (criteria != null) {
            audit.setHospitalId((String) criteria.get("hospitalId"));
            audit.setServiceCategory((String) criteria.get("serviceCategory"));
            audit.setPatientCategory((String) criteria.get("patientCategory"));
            audit.setGender((String) criteria.get("gender"));
            audit.setMinAge((Integer) criteria.get("minAge"));
            audit.setMaxAge((Integer) criteria.get("maxAge"));
        }

        try {
            IReportDataProvider provider = providerMap.get(reportType);
            if (provider == null)
                throw new RuntimeException("Unknown reportType: " + reportType);

            Map<String, Object> data = provider.fetchData(criteria);

            Map<String, Object> meta = new HashMap<>();
            meta.put("criteria", criteria);
            meta.put("filters", filters);
            meta.put("title", reportType + " Report");

            String html = reportGenerator.generate(data, meta);

            audit.setSuccess(true);
            auditRepository.save(audit);

            return new ReportResponse(html, meta);

        } catch (Exception ex) {
            audit.setNotes("Error: " + ex.getMessage());
            audit.setSuccess(false);
            auditRepository.save(audit);
            throw ex;
        }
    }

    public byte[] exportReportToPdf(String html) {
        return pdfExporter.export(html);
    }

    // Simple DTO for returning report data
    public static class ReportResponse {
        private String html;
        private Map<String, Object> meta;

        public ReportResponse(String html, Map<String, Object> meta) {
            this.html = html;
            this.meta = meta;
        }

        public String getHtml() { return html; }
        public Map<String, Object> getMeta() { return meta; }
        public void setHtml(String html) { this.html = html; }
        public void setMeta(Map<String, Object> meta) { this.meta = meta; }
    }
}
