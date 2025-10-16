package com.lankamed.health.backend.service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lankamed.health.backend.model.ReportAudit;
import com.lankamed.health.backend.model.Role;
import com.lankamed.health.backend.model.User;
import com.lankamed.health.backend.repository.ReportAuditRepository;
import com.lankamed.health.backend.repository.UserRepository;
import com.lankamed.health.backend.service.interfaces.IPdfExporter;
import com.lankamed.health.backend.service.interfaces.IReportDataProvider;
import com.lankamed.health.backend.service.interfaces.IReportGenerator;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private Map<String, IReportDataProvider> providerMap;
    @Mock
    private IReportGenerator reportGenerator;
    @Mock
    private IPdfExporter pdfExporter;
    @Mock
    private ReportAuditRepository auditRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private IReportDataProvider mockProvider;

    @InjectMocks
    private ReportService reportService;

    private User testUser;
    private Map<String, Object> testCriteria;
    private Map<String, Object> testFilters;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1L)
                .email("admin@lankamed.com")
                .firstName("Admin")
                .lastName("User")
                .role(Role.ADMIN)
                .createdAt(Instant.now())
                .build();

        testCriteria = Map.of(
                "from", "2024-01-01",
                "to", "2024-01-31",
                "hospitalId", "C001",
                "serviceCategory", "OPD"
        );

        testFilters = Map.of(
                "includeCharts", true,
                "format", "detailed"
        );
    }

    // Positive Test Cases
    @Test
    void createReport_Success() {
        // Given
        Map<String, Object> mockData = Map.of(
                "totalVisits", 150L,
                "uniquePatients", 120L
        );
        String mockHtml = "<html>Report Content</html>";

        when(userRepository.findByEmail("admin@lankamed.com"))
                .thenReturn(Optional.of(testUser));
        when(providerMap.get("PATIENT_VISIT")).thenReturn(mockProvider);
        when(mockProvider.fetchData(testCriteria)).thenReturn(mockData);
        when(reportGenerator.generate(eq(mockData), any(Map.class))).thenReturn(mockHtml);
        when(auditRepository.save(any(ReportAudit.class))).thenReturn(new ReportAudit());

        // When
        ReportService.ReportResponse result = reportService.createReport(
                "PATIENT_VISIT", testCriteria, testFilters, "admin@lankamed.com"
        );

        // Then
        assertNotNull(result);
        assertEquals(mockHtml, result.getHtml());
        assertNotNull(result.getMeta());
        assertEquals("PATIENT_VISIT Report", result.getMeta().get("title"));

        verify(auditRepository, times(1)).save(any(ReportAudit.class)); // Once for success
        verify(mockProvider).fetchData(testCriteria);
        verify(reportGenerator).generate(eq(mockData), any(Map.class));
    }

    @Test
    void createReport_WithAllCriteriaFields() {
        // Given
        Map<String, Object> fullCriteria = Map.of(
                "from", "2024-01-01",
                "to", "2024-01-31",
                "hospitalId", "C001",
                "serviceCategory", "OPD",
                "patientCategory", "OUTPATIENT",
                "gender", "MALE",
                "minAge", 18,
                "maxAge", 65
        );

        when(userRepository.findByEmail("admin@lankamed.com"))
                .thenReturn(Optional.of(testUser));
        when(providerMap.get("PATIENT_VISIT")).thenReturn(mockProvider);
        when(mockProvider.fetchData(fullCriteria)).thenReturn(Map.of("totalVisits", 100L));
        when(reportGenerator.generate(any(), any())).thenReturn("<html>Report</html>");
        when(auditRepository.save(any(ReportAudit.class))).thenReturn(new ReportAudit());

        // When
        ReportService.ReportResponse result = reportService.createReport(
                "PATIENT_VISIT", fullCriteria, testFilters, "admin@lankamed.com"
        );

        // Then
        assertNotNull(result);
        verify(auditRepository).save(argThat(audit -> 
                "C001".equals(audit.getHospitalId()) &&
                "OPD".equals(audit.getServiceCategory()) &&
                "OUTPATIENT".equals(audit.getPatientCategory()) &&
                "MALE".equals(audit.getGender()) &&
                Integer.valueOf(18).equals(audit.getMinAge()) &&
                Integer.valueOf(65).equals(audit.getMaxAge())
        ));
    }

    // Negative Test Cases
    @Test
    void createReport_UnknownReportType_ThrowsException() {
        // Given
        when(userRepository.findByEmail("admin@lankamed.com"))
                .thenReturn(Optional.of(testUser));
        when(providerMap.get("UNKNOWN_TYPE")).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reportService.createReport("UNKNOWN_TYPE", testCriteria, testFilters, "admin@lankamed.com");
        });

        assertEquals("Unknown reportType: UNKNOWN_TYPE", exception.getMessage());
        verify(auditRepository).save(argThat(audit -> !audit.isSuccess()));
    }

    @Test
    void createReport_UserNotFound_StillCreatesAudit() {
        // Given
        when(userRepository.findByEmail("nonexistent@lankamed.com"))
                .thenReturn(Optional.empty());
        when(providerMap.get("PATIENT_VISIT")).thenReturn(mockProvider);
        when(mockProvider.fetchData(testCriteria)).thenReturn(Map.of("totalVisits", 100L));
        when(reportGenerator.generate(any(), any())).thenReturn("<html>Report</html>");
        when(auditRepository.save(any(ReportAudit.class))).thenReturn(new ReportAudit());

        // When
        ReportService.ReportResponse result = reportService.createReport(
                "PATIENT_VISIT", testCriteria, testFilters, "nonexistent@lankamed.com"
        );

        // Then
        assertNotNull(result);
        verify(auditRepository).save(argThat(audit -> audit.getUser() == null));
    }

    // Edge Cases
    @Test
    void createReport_NullCriteria_HandlesGracefully() {
        // Given
        when(userRepository.findByEmail("admin@lankamed.com"))
                .thenReturn(Optional.of(testUser));
        when(providerMap.get("PATIENT_VISIT")).thenReturn(mockProvider);
        when(mockProvider.fetchData(null)).thenReturn(Map.of("totalVisits", 0L));
        when(reportGenerator.generate(any(), any())).thenReturn("<html>Report</html>");
        when(auditRepository.save(any(ReportAudit.class))).thenReturn(new ReportAudit());

        // When
        ReportService.ReportResponse result = reportService.createReport(
                "PATIENT_VISIT", null, null, "admin@lankamed.com"
        );

        // Then
        assertNotNull(result);
        verify(auditRepository).save(argThat(audit -> "{}".equals(audit.getCriteriaJson())));
    }

    @Test
    void createReport_EmptyCriteria_HandlesGracefully() {
        // Given
        Map<String, Object> emptyCriteria = Map.of();
        when(userRepository.findByEmail("admin@lankamed.com"))
                .thenReturn(Optional.of(testUser));
        when(providerMap.get("PATIENT_VISIT")).thenReturn(mockProvider);
        when(mockProvider.fetchData(emptyCriteria)).thenReturn(Map.of("totalVisits", 0L));
        when(reportGenerator.generate(any(), any())).thenReturn("<html>Report</html>");
        when(auditRepository.save(any(ReportAudit.class))).thenReturn(new ReportAudit());

        // When
        ReportService.ReportResponse result = reportService.createReport(
                "PATIENT_VISIT", emptyCriteria, testFilters, "admin@lankamed.com"
        );

        // Then
        assertNotNull(result);
        verify(auditRepository).save(argThat(audit -> 
                audit.getHospitalId() == null &&
                audit.getServiceCategory() == null &&
                audit.getPatientCategory() == null
        ));
    }

    // Error Cases
    @Test
    void createReport_DataProviderThrowsException_AuditsFailure() {
        // Given
        when(userRepository.findByEmail("admin@lankamed.com"))
                .thenReturn(Optional.of(testUser));
        when(providerMap.get("PATIENT_VISIT")).thenReturn(mockProvider);
        when(mockProvider.fetchData(testCriteria))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reportService.createReport("PATIENT_VISIT", testCriteria, testFilters, "admin@lankamed.com");
        });

        assertEquals("Database connection failed", exception.getMessage());
        verify(auditRepository).save(argThat(audit -> 
                !audit.isSuccess() && 
                audit.getNotes().contains("Database connection failed")
        ));
    }

    @Test
    void createReport_ReportGeneratorThrowsException_AuditsFailure() {
        // Given
        when(userRepository.findByEmail("admin@lankamed.com"))
                .thenReturn(Optional.of(testUser));
        when(providerMap.get("PATIENT_VISIT")).thenReturn(mockProvider);
        when(mockProvider.fetchData(testCriteria)).thenReturn(Map.of("totalVisits", 100L));
        when(reportGenerator.generate(any(), any()))
                .thenThrow(new RuntimeException("HTML generation failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reportService.createReport("PATIENT_VISIT", testCriteria, testFilters, "admin@lankamed.com");
        });

        assertEquals("HTML generation failed", exception.getMessage());
        verify(auditRepository).save(argThat(audit -> 
                !audit.isSuccess() && 
                audit.getNotes().contains("HTML generation failed")
        ));
    }

    // PDF Export Tests
    @Test
    void exportReportToPdf_Success() {
        // Given
        String htmlContent = "<html><body>Test Report</body></html>";
        byte[] expectedPdf = "PDF_CONTENT".getBytes();
        when(pdfExporter.export(htmlContent)).thenReturn(expectedPdf);

        // When
        byte[] result = reportService.exportReportToPdf(htmlContent);

        // Then
        assertArrayEquals(expectedPdf, result);
        verify(pdfExporter).export(htmlContent);
    }

    @Test
    void exportReportToPdf_PdfExporterThrowsException() {
        // Given
        String htmlContent = "<html><body>Test Report</body></html>";
        when(pdfExporter.export(htmlContent))
                .thenThrow(new RuntimeException("PDF generation failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reportService.exportReportToPdf(htmlContent);
        });

        assertEquals("PDF generation failed", exception.getMessage());
    }
}
