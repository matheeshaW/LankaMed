package com.lankamed.health.backend.service;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HtmlReportGeneratorTest {

    @InjectMocks
    private HtmlReportGenerator reportGenerator;

    // Positive Test Cases
    @Test
    void generate_WithCompleteData_ReturnsValidHtml() {
        // Given
        Map<String, Object> data = Map.of(
                "totalVisits", 150L,
                "uniquePatients", 120L,
                "averageVisitsPerPatient", 1.25
        );

        Map<String, Object> meta = Map.of(
                "title", "Patient Visit Report",
                "criteria", Map.of(
                        "from", "2024-01-01",
                        "to", "2024-01-31",
                        "hospitalId", "C001",
                        "serviceCategory", "OPD",
                        "gender", "MALE",
                        "minAge", 18,
                        "maxAge", 65
                )
        );

        // When
        String result = reportGenerator.generate(data, meta);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("<!DOCTYPE html"));
        assertTrue(result.contains("Patient Visit Report"));
        assertTrue(result.contains("150")); // totalVisits
        assertTrue(result.contains("120")); // uniquePatients
        assertTrue(result.contains("C001")); // hospitalId
        assertTrue(result.contains("OPD")); // serviceCategory
        assertTrue(result.contains("MALE")); // gender
        assertTrue(result.contains("18 - 65")); // age range
        assertTrue(result.contains("LankaMed Healthcare System"));
    }

    @Test
    void generate_WithMinimalData_ReturnsValidHtml() {
        // Given
        Map<String, Object> data = Map.of("totalVisits", 0L);
        Map<String, Object> meta = Map.of(
                "title", "Simple Report",
                "criteria", Map.of()
        );

        // When
        String result = reportGenerator.generate(data, meta);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Simple Report"));
        assertTrue(result.contains("0")); // totalVisits
        assertTrue(result.contains("All")); // default values
    }

    @Test
    void generate_WithMultipleKpis_DisplaysCorrectly() {
        // Given
        Map<String, Object> data = Map.of(
                "totalVisits", 100L,
                "uniquePatients", 80L,
                "averageVisitsPerPatient", 1.25,
                "totalRevenue", 50000.0
        );

        Map<String, Object> meta = Map.of(
                "title", "Comprehensive Report",
                "criteria", Map.of("from", "2024-01-01", "to", "2024-01-31")
        );

        // When
        String result = reportGenerator.generate(data, meta);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("100"));
        assertTrue(result.contains("80"));
        assertTrue(result.contains("1.25"));
        assertTrue(result.contains("50000.0"));
    }

    @Test
    void generate_WithAllCriteriaFields_DisplaysAll() {
        // Given
        Map<String, Object> data = Map.of("totalVisits", 50L);
        Map<String, Object> meta = Map.of(
                "title", "Detailed Report",
                "criteria", Map.of(
                        "from", "2024-01-01",
                        "to", "2024-01-31",
                        "hospitalId", "C001",
                        "serviceCategory", "OPD",
                        "patientCategory", "OUTPATIENT",
                        "gender", "FEMALE",
                        "minAge", 25,
                        "maxAge", 45
                )
        );

        // When
        String result = reportGenerator.generate(data, meta);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("C001"));
        assertTrue(result.contains("OPD"));
        assertTrue(result.contains("OUTPATIENT"));
        assertTrue(result.contains("FEMALE"));
        assertTrue(result.contains("25 - 45"));
    }

    // Edge Cases
    @Test
    void generate_WithNullMeta_ThrowsException() {
        // Given
        Map<String, Object> data = Map.of("totalVisits", 100L);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            reportGenerator.generate(data, null);
        });
    }

    @Test
    void generate_WithEmptyData_ReturnsValidHtml() {
        // Given
        Map<String, Object> data = Map.of();
        Map<String, Object> meta = Map.of("title", "Empty Report");

        // When
        String result = reportGenerator.generate(data, meta);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Empty Report"));
        assertTrue(result.contains("LankaMed Healthcare System"));
    }

    @Test
    void generate_WithNullData_ThrowsException() {
        // Given
        Map<String, Object> meta = Map.of("title", "Null Data Report");

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            reportGenerator.generate(null, meta);
        });
    }

    @Test
    void generate_WithEmptyMeta_HandlesGracefully() {
        // Given
        Map<String, Object> data = Map.of("totalVisits", 100L);
        Map<String, Object> meta = Map.of();

        // When
        String result = reportGenerator.generate(data, meta);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Statistical Report")); // default title
        assertTrue(result.contains("100"));
    }

    @Test
    void generate_WithNullCriteria_HandlesGracefully() {
        // Given
        Map<String, Object> data = Map.of("totalVisits", 100L);
        Map<String, Object> meta = new java.util.HashMap<>();
        meta.put("title", "No Criteria Report");
        meta.put("criteria", null);

        // When
        String result = reportGenerator.generate(data, meta);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("No Criteria Report"));
        assertTrue(result.contains("All")); // default values for missing criteria
    }

    @Test
    void generate_WithPartialCriteria_HandlesGracefully() {
        // Given
        Map<String, Object> data = Map.of("totalVisits", 100L);
        Map<String, Object> meta = Map.of(
                "title", "Partial Criteria Report",
                "criteria", Map.of(
                        "from", "2024-01-01",
                        "hospitalId", "C001"
                        // Missing to, serviceCategory, etc.
                )
        );

        // When
        String result = reportGenerator.generate(data, meta);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Partial Criteria Report"));
        assertTrue(result.contains("2024-01-01"));
        assertTrue(result.contains("C001"));
        assertTrue(result.contains("All")); // for missing criteria
    }

    @Test
    void generate_WithOnlyFromDate_HandlesCorrectly() {
        // Given
        Map<String, Object> data = Map.of("totalVisits", 100L);
        Map<String, Object> meta = Map.of(
                "title", "From Date Only Report",
                "criteria", Map.of("from", "2024-01-01")
        );

        // When
        String result = reportGenerator.generate(data, meta);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("2024-01-01 to N/A"));
    }

    @Test
    void generate_WithOnlyToDate_HandlesCorrectly() {
        // Given
        Map<String, Object> data = Map.of("totalVisits", 100L);
        Map<String, Object> meta = Map.of(
                "title", "To Date Only Report",
                "criteria", Map.of("to", "2024-01-31")
        );

        // When
        String result = reportGenerator.generate(data, meta);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("N/A to 2024-01-31"));
    }

    @Test
    void generate_WithOnlyMinAge_HandlesCorrectly() {
        // Given
        Map<String, Object> data = Map.of("totalVisits", 100L);
        Map<String, Object> meta = Map.of(
                "title", "Min Age Only Report",
                "criteria", Map.of("minAge", 18)
        );

        // When
        String result = reportGenerator.generate(data, meta);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("18 - ∞"));
    }

    @Test
    void generate_WithOnlyMaxAge_HandlesCorrectly() {
        // Given
        Map<String, Object> data = Map.of("totalVisits", 100L);
        Map<String, Object> meta = Map.of(
                "title", "Max Age Only Report",
                "criteria", Map.of("maxAge", 65)
        );

        // When
        String result = reportGenerator.generate(data, meta);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("0 - 65"));
    }

    @Test
    void generate_WithSpecialCharacters_HandlesGracefully() {
        // Given
        Map<String, Object> data = Map.of("totalVisits", 100L);
        Map<String, Object> meta = Map.of(
                "title", "Report with <script>alert('xss')</script>",
                "criteria", Map.of(
                        "hospitalId", "C001 & Associates",
                        "serviceCategory", "OPD \"Special\" Services"
                )
        );

        // When
        String result = reportGenerator.generate(data, meta);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Report with"));
        assertTrue(result.contains("C001"));
        assertTrue(result.contains("OPD"));
        // Note: The application may or may not escape HTML, so we test for basic content presence
    }

    @Test
    void generate_WithNullValuesInCriteria_HandlesGracefully() {
        // Given
        Map<String, Object> data = Map.of("totalVisits", 100L);
        Map<String, Object> criteria = new java.util.HashMap<>();
        criteria.put("from", "2024-01-01");
        criteria.put("to", "2024-01-31");
        criteria.put("hospitalId", null);
        criteria.put("serviceCategory", null);
        criteria.put("gender", null);
        criteria.put("minAge", null);
        criteria.put("maxAge", null);
        
        Map<String, Object> meta = new java.util.HashMap<>();
        meta.put("title", "Null Values Report");
        meta.put("criteria", criteria);

        // When
        String result = reportGenerator.generate(data, meta);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("2024-01-01 to 2024-01-31"));
        assertTrue(result.contains("All")); // for null values
    }

    @Test
    void generate_WithOddNumberOfKpis_HandlesCorrectly() {
        // Given
        Map<String, Object> data = Map.of(
                "totalVisits", 100L,
                "uniquePatients", 80L,
                "averageVisitsPerPatient", 1.25
        ); // 3 KPIs - odd number

        Map<String, Object> meta = Map.of("title", "Odd KPI Report");

        // When
        String result = reportGenerator.generate(data, meta);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("100"));
        assertTrue(result.contains("80"));
        assertTrue(result.contains("1.25"));
        // Should handle odd number of KPIs correctly in table structure
    }

    @Test
    void generate_WithSingleKpi_HandlesCorrectly() {
        // Given
        Map<String, Object> data = Map.of("totalVisits", 100L);
        Map<String, Object> meta = Map.of("title", "Single KPI Report");

        // When
        String result = reportGenerator.generate(data, meta);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("100"));
    }

    @Test
    void generate_WithLargeNumbers_HandlesCorrectly() {
        // Given
        Map<String, Object> data = Map.of(
                "totalVisits", 999999L,
                "uniquePatients", 888888L,
                "totalRevenue", 1234567.89
        );

        Map<String, Object> meta = Map.of("title", "Large Numbers Report");

        // When
        String result = reportGenerator.generate(data, meta);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("999999"));
        assertTrue(result.contains("888888"));
        assertTrue(result.contains("1234567.89"));
    }

    @Test
    void generate_WithDecimalValues_HandlesCorrectly() {
        // Given
        Map<String, Object> data = Map.of(
                "averageVisitsPerPatient", 1.25,
                "conversionRate", 0.85,
                "satisfactionScore", 4.5
        );

        Map<String, Object> meta = Map.of("title", "Decimal Values Report");

        // When
        String result = reportGenerator.generate(data, meta);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("1.25"));
        assertTrue(result.contains("0.85"));
        assertTrue(result.contains("4.5"));
    }

    // Error Cases
    @Test
    void generate_WithInvalidData_HandlesGracefully() {
        // Given
        Map<String, Object> data = Map.of("totalVisits", new Object()); // Invalid data type
        Map<String, Object> meta = Map.of("title", "Test Report");

        // When
        String result = reportGenerator.generate(data, meta);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Test Report"));
    }

    @Test
    void generate_WithCircularReference_HandlesGracefully() {
        // Given
        Map<String, Object> data = Map.of("totalVisits", 100L);
        Map<String, Object> criteria = new java.util.HashMap<>();
        criteria.put("from", "2024-01-01");
        criteria.put("self", criteria); // Circular reference
        
        Map<String, Object> meta = new java.util.HashMap<>();
        meta.put("title", "Circular Reference Report");
        meta.put("criteria", criteria);

        // When
        String result = reportGenerator.generate(data, meta);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Circular Reference Report"));
        assertTrue(result.contains("2024-01-01"));
    }
}

