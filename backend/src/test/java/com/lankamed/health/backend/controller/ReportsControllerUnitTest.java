package com.lankamed.health.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.lankamed.health.backend.service.ReportService;
import com.lankamed.health.backend.service.ReportService.ReportResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportsController Unit Tests")
class ReportsControllerUnitTest {

    @Mock
    private ReportService reportService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ReportsController reportsController;


    @Nested
    @DisplayName("Report Generation Tests")
    class ReportGenerationTests {

        @Test
        @DisplayName("Should generate report successfully with valid request")
        void generateReport_Success_ReturnsReportResponse() {
            // Given
            when(authentication.getName()).thenReturn("admin@test.com");
            ReportsController.ReportRequest request = new ReportsController.ReportRequest();
            request.setReportType("PATIENT_VISIT");
            request.setCriteria(Map.of("from", "2024-01-01", "to", "2024-01-31"));
            request.setFilters(Map.of("includeCharts", true));

            ReportService.ReportResponse mockResponse = new ReportService.ReportResponse(
                    "<html>Report Content</html>",
                    Map.of("title", "Patient Visit Report", "criteria", request.getCriteria())
            );

            when(reportService.createReport(
                    "PATIENT_VISIT",
                    request.getCriteria(),
                    request.getFilters(),
                    "admin@test.com"
            )).thenReturn(mockResponse);

            // When
            ResponseEntity<ReportResponse> response = reportsController.generateReport(request, authentication);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("<html>Report Content</html>", response.getBody().getHtml());
            assertNotNull(response.getBody().getMeta());
            assertEquals("Patient Visit Report", response.getBody().getMeta().get("title"));
            assertNotNull(response.getBody().getMeta().get("criteria"));
            
            verify(reportService).createReport("PATIENT_VISIT", request.getCriteria(), request.getFilters(), "admin@test.com");
        }

        @Test
        @DisplayName("Should handle report generation with all filter fields")
        void generateReport_WithAllFields_ReturnsReportResponse() {
            // Given
            when(authentication.getName()).thenReturn("admin@test.com");
            ReportsController.ReportRequest request = new ReportsController.ReportRequest();
            request.setReportType("SERVICE_UTILIZATION");
            request.setCriteria(Map.of(
                    "from", "2024-01-01",
                    "to", "2024-01-31",
                    "hospitalId", "C001",
                    "serviceCategory", "OPD",
                    "patientCategory", "OUTPATIENT",
                    "gender", "MALE",
                    "minAge", 18,
                    "maxAge", 65
            ));
            request.setFilters(Map.of(
                    "includeCharts", true,
                    "format", "detailed",
                    "groupBy", "service"
            ));

            ReportService.ReportResponse mockResponse = new ReportService.ReportResponse(
                    "<html>Service Utilization Report</html>",
                    Map.of("title", "Service Utilization Report")
            );

            when(reportService.createReport(
                    "SERVICE_UTILIZATION",
                    request.getCriteria(),
                    request.getFilters(),
                    "admin@test.com"
            )).thenReturn(mockResponse);

            // When
            ResponseEntity<ReportResponse> response = reportsController.generateReport(request, authentication);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("<html>Service Utilization Report</html>", response.getBody().getHtml());
            assertNotNull(response.getBody().getMeta());
            assertEquals("Service Utilization Report", response.getBody().getMeta().get("title"));
            
            verify(reportService).createReport("SERVICE_UTILIZATION", request.getCriteria(), request.getFilters(), "admin@test.com");
        }

        @Test
        @DisplayName("Should handle null criteria and filters gracefully")
        void generateReport_WithNullCriteriaAndFilters_HandlesGracefully() {
            // Given
            when(authentication.getName()).thenReturn("admin@test.com");
            ReportsController.ReportRequest request = new ReportsController.ReportRequest();
            request.setReportType("PATIENT_VISIT");
            request.setCriteria(null);
            request.setFilters(null);

            ReportService.ReportResponse mockResponse = new ReportService.ReportResponse(
                    "<html>Null Criteria Report</html>",
                    Map.of("title", "Null Criteria Report")
            );

            when(reportService.createReport(any(), any(), any(), any())).thenReturn(mockResponse);

            // When
            ResponseEntity<ReportResponse> response = reportsController.generateReport(request, authentication);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("<html>Null Criteria Report</html>", response.getBody().getHtml());
            
            verify(reportService).createReport("PATIENT_VISIT", null, null, "admin@test.com");
        }

        @Test
        @DisplayName("Should handle empty report type")
        void generateReport_WithEmptyReportType_HandlesGracefully() {
            // Given
            when(authentication.getName()).thenReturn("admin@test.com");
            ReportsController.ReportRequest request = new ReportsController.ReportRequest();
            request.setReportType("");
            request.setCriteria(Map.of("from", "2024-01-01", "to", "2024-01-31"));

            ReportService.ReportResponse mockResponse = new ReportService.ReportResponse(
                    "<html>Empty Report Type</html>",
                    Map.of("title", "Empty Report")
            );

            when(reportService.createReport(any(), any(), any(), any())).thenReturn(mockResponse);

            // When
            ResponseEntity<ReportResponse> response = reportsController.generateReport(request, authentication);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("<html>Empty Report Type</html>", response.getBody().getHtml());
        }

        @Test
        @DisplayName("Should handle null report type")
        void generateReport_WithNullReportType_HandlesGracefully() {
            // Given
            when(authentication.getName()).thenReturn("admin@test.com");
            ReportsController.ReportRequest request = new ReportsController.ReportRequest();
            request.setReportType(null);
            request.setCriteria(Map.of("from", "2024-01-01", "to", "2024-01-31"));

            when(reportService.createReport(any(), any(), any(), any()))
                    .thenThrow(new RuntimeException("Report type cannot be null"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                reportsController.generateReport(request, authentication);
            });
        }

        @Test
        @DisplayName("Should handle service exception during report generation")
        void generateReport_ServiceThrowsException_ThrowsRuntimeException() {
            // Given
            when(authentication.getName()).thenReturn("admin@test.com");
            ReportsController.ReportRequest request = new ReportsController.ReportRequest();
            request.setReportType("PATIENT_VISIT");

            when(reportService.createReport(any(), any(), any(), any()))
                    .thenThrow(new RuntimeException("Service error"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                reportsController.generateReport(request, authentication);
            });
        }
    }

    @Nested
    @DisplayName("Report Download Tests")
    class ReportDownloadTests {

        @Test
        @DisplayName("Should download report successfully with HTML content")
        void downloadReport_WithHtml_Success() {
            // Given
            ReportsController.DownloadRequest request = new ReportsController.DownloadRequest();
            request.html = "<html>Report Content</html>";

            byte[] mockPdf = "PDF_CONTENT".getBytes();
            when(reportService.exportReportToPdf("<html>Report Content</html>"))
                    .thenReturn(mockPdf);

            // When
            ResponseEntity<byte[]> response = reportsController.downloadReport(request, authentication);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertArrayEquals(mockPdf, response.getBody());
            assertEquals("attachment; filename=report.pdf", response.getHeaders().getFirst("Content-Disposition"));
            assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
            assertEquals("no-cache, no-store, must-revalidate", response.getHeaders().getFirst("Cache-Control"));
            assertEquals("no-cache", response.getHeaders().getFirst("Pragma"));
            assertEquals("0", response.getHeaders().getFirst("Expires"));
            
            verify(reportService).exportReportToPdf("<html>Report Content</html>");
        }

        @Test
        @DisplayName("Should download report successfully with report type")
        void downloadReport_WithReportType_Success() {
            // Given
            when(authentication.getName()).thenReturn("admin@test.com");
            ReportsController.DownloadRequest request = new ReportsController.DownloadRequest();
            request.reportType = "PATIENT_VISIT";
            request.criteria = Map.of("from", "2024-01-01", "to", "2024-01-31");
            request.filters = Map.of("includeCharts", true);

            ReportService.ReportResponse mockReportResponse = new ReportService.ReportResponse(
                    "<html>Generated Report</html>",
                    Map.of("title", "Patient Visit Report")
            );
            byte[] mockPdf = "PDF_CONTENT".getBytes();

            when(reportService.createReport(
                    "PATIENT_VISIT",
                    request.criteria,
                    request.filters,
                    "admin@test.com"
            )).thenReturn(mockReportResponse);
            when(reportService.exportReportToPdf("<html>Generated Report</html>"))
                    .thenReturn(mockPdf);

            // When
            ResponseEntity<byte[]> response = reportsController.downloadReport(request, authentication);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertArrayEquals(mockPdf, response.getBody());
            
            verify(reportService).createReport("PATIENT_VISIT", request.criteria, request.filters, "admin@test.com");
            verify(reportService).exportReportToPdf("<html>Generated Report</html>");
        }

        @Test
        @DisplayName("Should prefer HTML over report type when both are provided")
        void downloadReport_WithBothHtmlAndReportType_PrefersHtml() {
            // Given
            ReportsController.DownloadRequest request = new ReportsController.DownloadRequest();
            request.html = "<html>Direct HTML</html>";
            request.reportType = "PATIENT_VISIT";
            request.criteria = Map.of("from", "2024-01-01", "to", "2024-01-31");

            byte[] mockPdf = "PDF_CONTENT".getBytes();
            when(reportService.exportReportToPdf("<html>Direct HTML</html>"))
                    .thenReturn(mockPdf);

            // When
            ResponseEntity<byte[]> response = reportsController.downloadReport(request, authentication);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertArrayEquals(mockPdf, response.getBody());
            
            // Verify that report generation was not called since HTML was provided
            verify(reportService, never()).createReport(any(), any(), any(), any());
            verify(reportService).exportReportToPdf("<html>Direct HTML</html>");
        }

        @Test
        @DisplayName("Should return bad request when no HTML or report type provided")
        void downloadReport_NoHtmlOrReportType_ReturnsBadRequest() {
            // Given
            ReportsController.DownloadRequest request = new ReportsController.DownloadRequest();

            // When
            ResponseEntity<byte[]> response = reportsController.downloadReport(request, authentication);

            // Then
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNull(response.getBody());
        }

        @Test
        @DisplayName("Should return bad request when HTML is empty")
        void downloadReport_EmptyHtml_ReturnsBadRequest() {
            // Given
            ReportsController.DownloadRequest request = new ReportsController.DownloadRequest();
            request.html = ""; // Empty HTML

            // When
            ResponseEntity<byte[]> response = reportsController.downloadReport(request, authentication);

            // Then
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNull(response.getBody());
        }

        @Test
        @DisplayName("Should return bad request when HTML is whitespace only")
        void downloadReport_WhitespaceHtml_ReturnsBadRequest() {
            // Given
            ReportsController.DownloadRequest request = new ReportsController.DownloadRequest();
            request.html = "   \n\t  "; // Whitespace only HTML

            // When
            ResponseEntity<byte[]> response = reportsController.downloadReport(request, authentication);

            // Then
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNull(response.getBody());
        }

        @Test
        @DisplayName("Should handle PDF export failure")
        void downloadReport_PdfExportFails_ThrowsRuntimeException() {
            // Given
            ReportsController.DownloadRequest request = new ReportsController.DownloadRequest();
            request.html = "<html>Report</html>";

            when(reportService.exportReportToPdf(any()))
                    .thenThrow(new RuntimeException("PDF generation failed"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                reportsController.downloadReport(request, authentication);
            });
        }

        @Test
        @DisplayName("Should handle report generation failure during download")
        void downloadReport_ReportGenerationFails_ThrowsRuntimeException() {
            // Given
            when(authentication.getName()).thenReturn("admin@test.com");
            ReportsController.DownloadRequest request = new ReportsController.DownloadRequest();
            request.reportType = "PATIENT_VISIT";
            request.criteria = Map.of("from", "2024-01-01", "to", "2024-01-31");

            when(reportService.createReport(any(), any(), any(), any()))
                    .thenThrow(new RuntimeException("Report generation failed"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                reportsController.downloadReport(request, authentication);
            });
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Tests")
    class EdgeCasesAndBoundaryTests {

        @Test
        @DisplayName("Should handle very large criteria map")
        void generateReport_WithVeryLargeCriteria_HandlesGracefully() {
            // Given
            when(authentication.getName()).thenReturn("admin@test.com");
            ReportsController.ReportRequest request = new ReportsController.ReportRequest();
            request.setReportType("PATIENT_VISIT");
            
            // Create a large criteria map
            Map<String, Object> largeCriteria = new HashMap<>();
            for (int i = 0; i < 100; i++) {
                largeCriteria.put("field" + i, "value" + i);
            }
            largeCriteria.put("from", "2024-01-01");
            largeCriteria.put("to", "2024-01-31");
            request.setCriteria(largeCriteria);

            ReportService.ReportResponse mockResponse = new ReportService.ReportResponse(
                    "<html>Large Criteria Report</html>",
                    Map.of("title", "Large Criteria Report")
            );

            when(reportService.createReport(any(), any(), any(), any())).thenReturn(mockResponse);

            // When
            ResponseEntity<ReportResponse> response = reportsController.generateReport(request, authentication);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("<html>Large Criteria Report</html>", response.getBody().getHtml());
        }

        @Test
        @DisplayName("Should handle very large filters map")
        void generateReport_WithVeryLargeFilters_HandlesGracefully() {
            // Given
            when(authentication.getName()).thenReturn("admin@test.com");
            ReportsController.ReportRequest request = new ReportsController.ReportRequest();
            request.setReportType("PATIENT_VISIT");
            request.setCriteria(Map.of("from", "2024-01-01", "to", "2024-01-31"));
            
            Map<String, Object> largeFilters = new HashMap<>();
            for (int i = 0; i < 50; i++) {
                largeFilters.put("filter" + i, "value" + i);
            }
            request.setFilters(largeFilters);

            ReportService.ReportResponse mockResponse = new ReportService.ReportResponse(
                    "<html>Large Filters Report</html>",
                    Map.of("title", "Large Filters Report")
            );

            when(reportService.createReport(any(), any(), any(), any())).thenReturn(mockResponse);

            // When
            ResponseEntity<ReportResponse> response = reportsController.generateReport(request, authentication);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("<html>Large Filters Report</html>", response.getBody().getHtml());
        }

        @Test
        @DisplayName("Should handle nested objects in criteria")
        void generateReport_WithNestedObjectsInCriteria_HandlesCorrectly() {
            // Given
            when(authentication.getName()).thenReturn("admin@test.com");
            ReportsController.ReportRequest request = new ReportsController.ReportRequest();
            request.setReportType("PATIENT_VISIT");
            
            Map<String, Object> nestedCriteria = new HashMap<>();
            nestedCriteria.put("from", "2024-01-01");
            nestedCriteria.put("to", "2024-01-31");
            nestedCriteria.put("hospital", Map.of("id", "H001", "name", "Hospital 1"));
            nestedCriteria.put("patient", Map.of("category", "OUTPATIENT", "type", "REGULAR"));
            nestedCriteria.put("service", Map.of("category", "OPD", "department", "CARDIOLOGY"));
            request.setCriteria(nestedCriteria);

            ReportService.ReportResponse mockResponse = new ReportService.ReportResponse(
                    "<html>Nested Objects Report</html>",
                    Map.of("title", "Nested Objects Report")
            );

            when(reportService.createReport(any(), any(), any(), any())).thenReturn(mockResponse);

            // When
            ResponseEntity<ReportResponse> response = reportsController.generateReport(request, authentication);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("<html>Nested Objects Report</html>", response.getBody().getHtml());
        }

        @Test
        @DisplayName("Should handle list values in criteria")
        void generateReport_WithListValuesInCriteria_HandlesCorrectly() {
            // Given
            when(authentication.getName()).thenReturn("admin@test.com");
            ReportsController.ReportRequest request = new ReportsController.ReportRequest();
            request.setReportType("PATIENT_VISIT");
            
            Map<String, Object> listCriteria = new HashMap<>();
            listCriteria.put("from", "2024-01-01");
            listCriteria.put("to", "2024-01-31");
            listCriteria.put("hospitalIds", List.of("H001", "H002", "H003"));
            listCriteria.put("serviceCategories", List.of("OPD", "EMERGENCY", "SURGERY"));
            listCriteria.put("patientCategories", List.of("OUTPATIENT", "INPATIENT"));
            request.setCriteria(listCriteria);

            ReportService.ReportResponse mockResponse = new ReportService.ReportResponse(
                    "<html>List Values Report</html>",
                    Map.of("title", "List Values Report")
            );

            when(reportService.createReport(any(), any(), any(), any())).thenReturn(mockResponse);

            // When
            ResponseEntity<ReportResponse> response = reportsController.generateReport(request, authentication);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("<html>List Values Report</html>", response.getBody().getHtml());
        }

        @Test
        @DisplayName("Should handle boolean values in criteria")
        void generateReport_WithBooleanValuesInCriteria_HandlesCorrectly() {
            // Given
            when(authentication.getName()).thenReturn("admin@test.com");
            ReportsController.ReportRequest request = new ReportsController.ReportRequest();
            request.setReportType("PATIENT_VISIT");
            
            Map<String, Object> booleanCriteria = new HashMap<>();
            booleanCriteria.put("from", "2024-01-01");
            booleanCriteria.put("to", "2024-01-31");
            booleanCriteria.put("includeEmergency", true);
            booleanCriteria.put("includeSurgery", false);
            booleanCriteria.put("includeOPD", true);
            request.setCriteria(booleanCriteria);

            ReportService.ReportResponse mockResponse = new ReportService.ReportResponse(
                    "<html>Boolean Values Report</html>",
                    Map.of("title", "Boolean Values Report")
            );

            when(reportService.createReport(any(), any(), any(), any())).thenReturn(mockResponse);

            // When
            ResponseEntity<ReportResponse> response = reportsController.generateReport(request, authentication);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("<html>Boolean Values Report</html>", response.getBody().getHtml());
        }

        @Test
        @DisplayName("Should handle numeric values in criteria")
        void generateReport_WithNumericValuesInCriteria_HandlesCorrectly() {
            // Given
            when(authentication.getName()).thenReturn("admin@test.com");
            ReportsController.ReportRequest request = new ReportsController.ReportRequest();
            request.setReportType("PATIENT_VISIT");
            
            Map<String, Object> numericCriteria = new HashMap<>();
            numericCriteria.put("from", "2024-01-01");
            numericCriteria.put("to", "2024-01-31");
            numericCriteria.put("minAge", 18);
            numericCriteria.put("maxAge", 65);
            numericCriteria.put("minWeight", 50.5);
            numericCriteria.put("maxWeight", 100.0);
            numericCriteria.put("priority", 1);
            request.setCriteria(numericCriteria);

            ReportService.ReportResponse mockResponse = new ReportService.ReportResponse(
                    "<html>Numeric Values Report</html>",
                    Map.of("title", "Numeric Values Report")
            );

            when(reportService.createReport(any(), any(), any(), any())).thenReturn(mockResponse);

            // When
            ResponseEntity<ReportResponse> response = reportsController.generateReport(request, authentication);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("<html>Numeric Values Report</html>", response.getBody().getHtml());
        }

        @Test
        @DisplayName("Should handle special characters in report type")
        void generateReport_WithSpecialCharactersInReportType_HandlesCorrectly() {
            // Given
            when(authentication.getName()).thenReturn("admin@test.com");
            ReportsController.ReportRequest request = new ReportsController.ReportRequest();
            request.setReportType("REPORT_TYPE@#$%^&*()");
            request.setCriteria(Map.of("from", "2024-01-01", "to", "2024-01-31"));

            ReportService.ReportResponse mockResponse = new ReportService.ReportResponse(
                    "<html>Special Characters Report</html>",
                    Map.of("title", "Special Report")
            );

            when(reportService.createReport(any(), any(), any(), any())).thenReturn(mockResponse);

            // When
            ResponseEntity<ReportResponse> response = reportsController.generateReport(request, authentication);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("<html>Special Characters Report</html>", response.getBody().getHtml());
        }

        @Test
        @DisplayName("Should handle unicode characters in report type")
        void generateReport_WithUnicodeCharactersInReportType_HandlesCorrectly() {
            // Given
            when(authentication.getName()).thenReturn("admin@test.com");
            ReportsController.ReportRequest request = new ReportsController.ReportRequest();
            request.setReportType("REPORT_TYPE_你好世界");
            request.setCriteria(Map.of("from", "2024-01-01", "to", "2024-01-31"));

            ReportService.ReportResponse mockResponse = new ReportService.ReportResponse(
                    "<html>Unicode Report</html>",
                    Map.of("title", "Unicode Report")
            );

            when(reportService.createReport(any(), any(), any(), any())).thenReturn(mockResponse);

            // When
            ResponseEntity<ReportResponse> response = reportsController.generateReport(request, authentication);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("<html>Unicode Report</html>", response.getBody().getHtml());
        }

        @Test
        @DisplayName("Should handle whitespace in report type")
        void generateReport_WithWhitespaceInReportType_HandlesCorrectly() {
            // Given
            when(authentication.getName()).thenReturn("admin@test.com");
            ReportsController.ReportRequest request = new ReportsController.ReportRequest();
            request.setReportType("  REPORT_TYPE  ");
            request.setCriteria(Map.of("from", "2024-01-01", "to", "2024-01-31"));

            ReportService.ReportResponse mockResponse = new ReportService.ReportResponse(
                    "<html>Whitespace Report</html>",
                    Map.of("title", "Whitespace Report")
            );

            when(reportService.createReport(any(), any(), any(), any())).thenReturn(mockResponse);

            // When
            ResponseEntity<ReportResponse> response = reportsController.generateReport(request, authentication);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("<html>Whitespace Report</html>", response.getBody().getHtml());
        }
    }

    @Nested
    @DisplayName("DTO Tests")
    class DTOTests {

        @Test
        @DisplayName("Should test ReportRequest DTO getters and setters")
        void reportRequest_GetterSetterMethods_WorkCorrectly() {
            // Given
            ReportsController.ReportRequest request = new ReportsController.ReportRequest();
            String reportType = "PATIENT_VISIT";
            Map<String, Object> criteria = Map.of("from", "2024-01-01", "to", "2024-01-31");
            Map<String, Object> filters = Map.of("includeCharts", true);
            String hospitalId = "H001";
            String serviceCategory = "OPD";
            String patientCategory = "OUTPATIENT";
            String gender = "MALE";
            Integer minAge = 18;
            Integer maxAge = 65;

            // When
            request.setReportType(reportType);
            request.setCriteria(criteria);
            request.setFilters(filters);
            request.setHospitalId(hospitalId);
            request.setServiceCategory(serviceCategory);
            request.setPatientCategory(patientCategory);
            request.setGender(gender);
            request.setMinAge(minAge);
            request.setMaxAge(maxAge);

            // Then
            assertEquals(reportType, request.getReportType());
            assertEquals(criteria, request.getCriteria());
            assertEquals(filters, request.getFilters());
            assertEquals(hospitalId, request.getHospitalId());
            assertEquals(serviceCategory, request.getServiceCategory());
            assertEquals(patientCategory, request.getPatientCategory());
            assertEquals(gender, request.getGender());
            assertEquals(minAge, request.getMinAge());
            assertEquals(maxAge, request.getMaxAge());
        }

        @Test
        @DisplayName("Should test ReportRequest DTO with null values")
        void reportRequest_WithNullValues_HandlesCorrectly() {
            // Given
            ReportsController.ReportRequest request = new ReportsController.ReportRequest();

            // When
            request.setReportType(null);
            request.setCriteria(null);
            request.setFilters(null);
            request.setHospitalId(null);
            request.setServiceCategory(null);
            request.setPatientCategory(null);
            request.setGender(null);
            request.setMinAge(null);
            request.setMaxAge(null);

            // Then
            assertNull(request.getReportType());
            assertNull(request.getCriteria());
            assertNull(request.getFilters());
            assertNull(request.getHospitalId());
            assertNull(request.getServiceCategory());
            assertNull(request.getPatientCategory());
            assertNull(request.getGender());
            assertNull(request.getMinAge());
            assertNull(request.getMaxAge());
        }

        @Test
        @DisplayName("Should test DownloadRequest DTO fields")
        void downloadRequest_Fields_WorkCorrectly() {
            // Given
            ReportsController.DownloadRequest request = new ReportsController.DownloadRequest();
            String html = "<html>Test</html>";
            String reportType = "PATIENT_VISIT";
            Map<String, Object> criteria = Map.of("from", "2024-01-01", "to", "2024-01-31");
            Map<String, Object> filters = Map.of("includeCharts", true);

            // When
            request.html = html;
            request.reportType = reportType;
            request.criteria = criteria;
            request.filters = filters;

            // Then
            assertEquals(html, request.html);
            assertEquals(reportType, request.reportType);
            assertEquals(criteria, request.criteria);
            assertEquals(filters, request.filters);
        }

        @Test
        @DisplayName("Should test DownloadRequest DTO with null values")
        void downloadRequest_WithNullValues_HandlesCorrectly() {
            // Given
            ReportsController.DownloadRequest request = new ReportsController.DownloadRequest();

            // When
            request.html = null;
            request.reportType = null;
            request.criteria = null;
            request.filters = null;

            // Then
            assertNull(request.html);
            assertNull(request.reportType);
            assertNull(request.criteria);
            assertNull(request.filters);
        }
    }

    @Nested
    @DisplayName("Security and Validation Tests")
    class SecurityAndValidationTests {

        @Test
        @DisplayName("Should handle authentication with different user types")
        void generateReport_WithDifferentUserTypes_HandlesCorrectly() {
            // Given
            when(authentication.getName()).thenReturn("user@test.com");
            ReportsController.ReportRequest request = new ReportsController.ReportRequest();
            request.setReportType("PATIENT_VISIT");
            request.setCriteria(Map.of("from", "2024-01-01", "to", "2024-01-31"));

            ReportService.ReportResponse mockResponse = new ReportService.ReportResponse(
                    "<html>User Report</html>",
                    Map.of("title", "User Report")
            );

            when(reportService.createReport(any(), any(), any(), any())).thenReturn(mockResponse);

            // When
            ResponseEntity<ReportResponse> response = reportsController.generateReport(request, authentication);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(reportService).createReport("PATIENT_VISIT", request.getCriteria(), request.getFilters(), "user@test.com");
        }

        @Test
        @DisplayName("Should handle authentication with null name")
        void generateReport_WithNullAuthenticationName_HandlesCorrectly() {
            // Given
            when(authentication.getName()).thenReturn(null);
            ReportsController.ReportRequest request = new ReportsController.ReportRequest();
            request.setReportType("PATIENT_VISIT");
            request.setCriteria(Map.of("from", "2024-01-01", "to", "2024-01-31"));

            ReportService.ReportResponse mockResponse = new ReportService.ReportResponse(
                    "<html>Null Name Report</html>",
                    Map.of("title", "Null Name Report")
            );

            when(reportService.createReport(any(), any(), any(), any())).thenReturn(mockResponse);

            // When
            ResponseEntity<ReportResponse> response = reportsController.generateReport(request, authentication);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(reportService).createReport("PATIENT_VISIT", request.getCriteria(), request.getFilters(), null);
        }

        @Test
        @DisplayName("Should handle very long HTML content in download")
        void downloadReport_WithVeryLongHtml_HandlesCorrectly() {
            // Given
            StringBuilder longHtml = new StringBuilder("<html><body>");
            for (int i = 0; i < 10000; i++) {
                longHtml.append("<p>This is a very long HTML content line ").append(i).append("</p>");
            }
            longHtml.append("</body></html>");
            
            ReportsController.DownloadRequest request = new ReportsController.DownloadRequest();
            request.html = longHtml.toString();

            byte[] mockPdf = "LARGE_PDF_CONTENT".getBytes();
            when(reportService.exportReportToPdf(any())).thenReturn(mockPdf);

            // When
            ResponseEntity<byte[]> response = reportsController.downloadReport(request, authentication);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertArrayEquals(mockPdf, response.getBody());
            verify(reportService).exportReportToPdf(longHtml.toString());
        }

        @Test
        @DisplayName("Should handle HTML with special characters")
        void downloadReport_WithSpecialCharactersInHtml_HandlesCorrectly() {
            // Given
            String htmlWithSpecialChars = "<html><body><h1>Report with special chars: @#$%^&*()</h1><p>Unicode: 你好世界</p></body></html>";
            ReportsController.DownloadRequest request = new ReportsController.DownloadRequest();
            request.html = htmlWithSpecialChars;

            byte[] mockPdf = "SPECIAL_CHARS_PDF".getBytes();
            when(reportService.exportReportToPdf(any())).thenReturn(mockPdf);

            // When
            ResponseEntity<byte[]> response = reportsController.downloadReport(request, authentication);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertArrayEquals(mockPdf, response.getBody());
            verify(reportService).exportReportToPdf(htmlWithSpecialChars);
        }
    }

    @Nested
    @DisplayName("Performance and Stress Tests")
    class PerformanceAndStressTests {

        @Test
        @DisplayName("Should handle multiple concurrent report generations")
        void generateReport_MultipleConcurrentRequests_HandlesCorrectly() {
            // Given
            when(authentication.getName()).thenReturn("admin@test.com");
            ReportsController.ReportRequest request = new ReportsController.ReportRequest();
            request.setReportType("PATIENT_VISIT");
            request.setCriteria(Map.of("from", "2024-01-01", "to", "2024-01-31"));

            ReportService.ReportResponse mockResponse = new ReportService.ReportResponse(
                    "<html>Concurrent Report</html>",
                    Map.of("title", "Concurrent Report")
            );

            when(reportService.createReport(any(), any(), any(), any())).thenReturn(mockResponse);

            // When - Simulate multiple calls
            ResponseEntity<ReportResponse> response1 = reportsController.generateReport(request, authentication);
            ResponseEntity<ReportResponse> response2 = reportsController.generateReport(request, authentication);
            ResponseEntity<ReportResponse> response3 = reportsController.generateReport(request, authentication);

            // Then
            assertEquals(HttpStatus.OK, response1.getStatusCode());
            assertEquals(HttpStatus.OK, response2.getStatusCode());
            assertEquals(HttpStatus.OK, response3.getStatusCode());
            assertNotNull(response1.getBody());
            assertNotNull(response2.getBody());
            assertNotNull(response3.getBody());
            
            verify(reportService, times(3)).createReport(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should handle large PDF generation")
        void downloadReport_WithLargePdfGeneration_HandlesCorrectly() {
            // Given
            ReportsController.DownloadRequest request = new ReportsController.DownloadRequest();
            request.html = "<html>Large Report</html>";

            // Create a large byte array to simulate large PDF
            byte[] largePdf = new byte[1024 * 1024]; // 1MB
            for (int i = 0; i < largePdf.length; i++) {
                largePdf[i] = (byte) (i % 256);
            }

            when(reportService.exportReportToPdf(any())).thenReturn(largePdf);

            // When
            ResponseEntity<byte[]> response = reportsController.downloadReport(request, authentication);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(largePdf.length, response.getBody().length);
            assertArrayEquals(largePdf, response.getBody());
        }
    }

}
