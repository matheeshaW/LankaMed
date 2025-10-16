package com.lankamed.health.backend.controller;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lankamed.health.backend.service.ReportService;
import com.lankamed.health.backend.service.ReportService.ReportResponse;

@ExtendWith(MockitoExtension.class)
class ReportsControllerUnitTest {

    @Mock
    private ReportService reportService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ReportsController reportsController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    // Positive Test Cases
    @Test
    void generateReport_Success_ReturnsReportResponse() {
        // Given
        when(authentication.getName()).thenReturn("admin");
        ReportsController.ReportRequest request = new ReportsController.ReportRequest();
        request.setReportType("PATIENT_VISIT");
        request.setCriteria(Map.of("from", "2024-01-01", "to", "2024-01-31"));
        request.setFilters(Map.of("includeCharts", true));

        ReportService.ReportResponse mockResponse = new ReportService.ReportResponse(
                "<html>Report Content</html>",
                Map.of("title", "Patient Visit Report")
        );

        when(reportService.createReport(
                "PATIENT_VISIT",
                request.getCriteria(),
                request.getFilters(),
                "admin"
        )).thenReturn(mockResponse);

        // When
        ResponseEntity<ReportResponse> response = reportsController.generateReport(request, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("<html>Report Content</html>", response.getBody().getHtml());
        assertEquals("Patient Visit Report", response.getBody().getMeta().get("title"));
        
        verify(reportService).createReport("PATIENT_VISIT", request.getCriteria(), request.getFilters(), "admin");
    }

    @Test
    void generateReport_WithAllFields_ReturnsReportResponse() {
        // Given
        when(authentication.getName()).thenReturn("admin");
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
                "admin"
        )).thenReturn(mockResponse);

        // When
        ResponseEntity<ReportResponse> response = reportsController.generateReport(request, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("<html>Service Utilization Report</html>", response.getBody().getHtml());
        assertEquals("Service Utilization Report", response.getBody().getMeta().get("title"));
    }

    @Test
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
        assertEquals("application/pdf", response.getHeaders().getContentType().toString());
        
        verify(reportService).exportReportToPdf("<html>Report Content</html>");
    }

    @Test
    void downloadReport_WithReportType_Success() {
        // Given
        when(authentication.getName()).thenReturn("admin");
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
                "admin"
        )).thenReturn(mockReportResponse);
        when(reportService.exportReportToPdf("<html>Generated Report</html>"))
                .thenReturn(mockPdf);

        // When
        ResponseEntity<byte[]> response = reportsController.downloadReport(request, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertArrayEquals(mockPdf, response.getBody());
        
        verify(reportService).createReport("PATIENT_VISIT", request.criteria, request.filters, "admin");
        verify(reportService).exportReportToPdf("<html>Generated Report</html>");
    }

    // Negative Test Cases
    @Test
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

    // Error Cases
    @Test
    void generateReport_ServiceThrowsException_ThrowsRuntimeException() {
        // Given
        when(authentication.getName()).thenReturn("admin");
        ReportsController.ReportRequest request = new ReportsController.ReportRequest();
        request.setReportType("PATIENT_VISIT");

        when(reportService.createReport(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Service error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            reportsController.generateReport(request, authentication);
        });
    }

    @Test
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
    void downloadReport_ReportGenerationFails_ThrowsRuntimeException() {
        // Given
        when(authentication.getName()).thenReturn("admin");
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

    // Edge Cases
    @Test
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
    void generateReport_WithNullReportType_HandlesGracefully() {
        // Given
        when(authentication.getName()).thenReturn("admin");
        ReportsController.ReportRequest request = new ReportsController.ReportRequest();
        request.setReportType(null);
        request.setCriteria(Map.of("from", "2024-01-01", "to", "2024-01-31"));

        when(reportService.createReport(
                null,
                request.getCriteria(),
                request.getFilters(),
                "admin"
        )).thenThrow(new RuntimeException("Report type cannot be null"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            reportsController.generateReport(request, authentication);
        });
    }

    @Test
    void generateReport_WithEmptyReportType_HandlesGracefully() {
        // Given
        when(authentication.getName()).thenReturn("admin");
        ReportsController.ReportRequest request = new ReportsController.ReportRequest();
        request.reportType = "";
        request.setCriteria(Map.of("from", "2024-01-01", "to", "2024-01-31"));

        when(reportService.createReport(
                "",
                request.getCriteria(),
                request.getFilters(),
                "admin"
        )).thenThrow(new RuntimeException("Unknown reportType: "));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            reportsController.generateReport(request, authentication);
        });
    }

    @Test
    void generateReport_WithVeryLargeCriteria_HandlesGracefully() {
        // Given
        when(authentication.getName()).thenReturn("admin");
        ReportsController.ReportRequest request = new ReportsController.ReportRequest();
        request.setReportType("PATIENT_VISIT");
        
        // Create a large criteria map
        Map<String, Object> largeCriteria = new HashMap<>();
        largeCriteria.put("from", "2024-01-01");
        largeCriteria.put("to", "2024-01-31");
        largeCriteria.put("hospitalId", "C001");
        largeCriteria.put("serviceCategory", "OPD");
        largeCriteria.put("patientCategory", "OUTPATIENT");
        largeCriteria.put("gender", "MALE");
        largeCriteria.put("minAge", 18);
        largeCriteria.put("maxAge", 65);
        largeCriteria.put("additionalField1", "value1");
        largeCriteria.put("additionalField2", "value2");
        largeCriteria.put("additionalField3", "value3");
        request.setCriteria(largeCriteria);

        ReportService.ReportResponse mockResponse = new ReportService.ReportResponse(
                "<html>Large Criteria Report</html>",
                Map.of("title", "Patient Visit Report")
        );

        when(reportService.createReport(
                "PATIENT_VISIT",
                largeCriteria,
                request.getFilters(),
                "admin"
        )).thenReturn(mockResponse);

        // When
        ResponseEntity<ReportResponse> response = reportsController.generateReport(request, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("<html>Large Criteria Report</html>", response.getBody().getHtml());
    }
}
