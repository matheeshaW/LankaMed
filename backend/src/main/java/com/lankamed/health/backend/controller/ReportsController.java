package com.lankamed.health.backend.controller;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lankamed.health.backend.service.ReportService;
import com.lankamed.health.backend.service.ReportService.ReportResponse;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {
    private final ReportService reportService;

    public ReportsController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Request DTO for generating a report.
     */
    public static class ReportRequest {
        public String reportType;
        public Map<String, Object> criteria;
        public Map<String, Object> filters;

        // Add new filter fields
        private String hospitalId;
        private String serviceCategory;
        private String patientCategory;
        private String gender;
        private Integer minAge;
        private Integer maxAge;

        public String getReportType() { return reportType; }
        public Map<String, Object> getCriteria() { return criteria; }
        public Map<String, Object> getFilters() { return filters; }
        public void setReportType(String reportType) { this.reportType = reportType; }
        public void setCriteria(Map<String, Object> criteria) { this.criteria = criteria; }
        public void setFilters(Map<String, Object> filters) { this.filters = filters; }

        public String getHospitalId() { return hospitalId; }
        public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }
        public String getServiceCategory() { return serviceCategory; }
        public void setServiceCategory(String serviceCategory) { this.serviceCategory = serviceCategory; }
        public String getPatientCategory() { return patientCategory; }
        public void setPatientCategory(String patientCategory) { this.patientCategory = patientCategory; }
        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
        public Integer getMinAge() { return minAge; }
        public void setMinAge(Integer minAge) { this.minAge = minAge; }
        public Integer getMaxAge() { return maxAge; }
        public void setMaxAge(Integer maxAge) { this.maxAge = maxAge; }
    }

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportResponse> generateReport(@RequestBody ReportRequest request, Authentication authentication) {
        ReportResponse response = reportService.createReport(
            request.getReportType(),
            request.getCriteria(),
            request.getFilters(),
            authentication.getName()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Request DTO for downloading as PDF (can reuse criteria or supply html directly)
     */
    public static class DownloadRequest {
        public String html;
        public String reportType;
        public Map<String, Object> criteria;
        public Map<String, Object> filters;
    }

    @PostMapping("/download")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadReport(@RequestBody DownloadRequest request, Authentication authentication) {
        String html;
        if (request.html != null && !request.html.isBlank()) {
            html = request.html;
        } else if (request.reportType != null) {
            ReportResponse rr = reportService.createReport(request.reportType, request.criteria, request.filters, authentication.getName());
            html = rr.getHtml();
        } else {
            return ResponseEntity.badRequest().body(null);
        }
        byte[] bytes = reportService.exportReportToPdf(html);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.pdf");
        headers.setContentType(MediaType.APPLICATION_PDF);
        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}