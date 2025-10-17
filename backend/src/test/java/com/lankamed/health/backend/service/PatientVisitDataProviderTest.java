package com.lankamed.health.backend.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import com.lankamed.health.backend.model.Visit;
import com.lankamed.health.backend.model.patient.Patient;
import com.lankamed.health.backend.repository.VisitRepository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@ExtendWith(MockitoExtension.class)
class PatientVisitDataProviderTest {

    @Mock
    private VisitRepository visitRepository;

    @InjectMocks
    private PatientVisitDataProvider dataProvider;

    private List<Visit> mockVisits;

    @BeforeEach
    void setUp() {
        // Create mock visits with different patients
        Patient patient1 = new Patient();
        patient1.setPatientId(1L);
        Patient patient2 = new Patient();
        patient2.setPatientId(2L);
        
        Visit visit1 = new Visit();
        visit1.setPatient(patient1);
        visit1.setVisitDate(LocalDateTime.now());
        
        Visit visit2 = new Visit();
        visit2.setPatient(patient1);
        visit2.setVisitDate(LocalDateTime.now());
        
        Visit visit3 = new Visit();
        visit3.setPatient(patient2);
        visit3.setVisitDate(LocalDateTime.now());
        
        mockVisits = List.of(visit1, visit2, visit3);
    }

    // Positive Test Cases(Successful data fetching)
    @Test
    void fetchData_WithAllFilters_ReturnsCorrectData() {
        // Given
        Map<String, Object> criteria = Map.of(
                "hospitalId", "C001",
                "serviceCategory", "OPD",
                "patientCategory", "OUTPATIENT",
                "gender", "MALE",
                "minAge", 18,
                "maxAge", 65,
                "from", "2024-01-01",
                "to", "2024-01-31"
        );

        when(visitRepository.count(any(Specification.class))).thenReturn(150L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertNotNull(result);
        assertEquals(150L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients")); // 2 unique patients
        assertEquals("2024-01-01 - 2024-01-31", result.get("reportPeriod"));

        verify(visitRepository).count(any(Specification.class));
        verify(visitRepository).findAll(any(Specification.class));
    }

    @Test
    void fetchData_WithDateRangeOnly_ReturnsCorrectData() {
        // Given
        Map<String, Object> criteria = Map.of(
                "from", "2024-01-01",
                "to", "2024-01-31"
        );

        when(visitRepository.count(any(Specification.class))).thenReturn(75L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(75L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithHospitalFilter_ReturnsCorrectData() {
        // Given
        Map<String, Object> criteria = Map.of(
                "hospitalId", "C001",
                "from", "2024-01-01",
                "to", "2024-01-31"
        );

        when(visitRepository.count(any(Specification.class))).thenReturn(50L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(50L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithServiceCategoryFilter_ReturnsCorrectData() {
        // Given
        Map<String, Object> criteria = Map.of(
                "serviceCategory", "OPD",
                "from", "2024-01-01",
                "to", "2024-01-31"
        );

        when(visitRepository.count(any(Specification.class))).thenReturn(25L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(25L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithAgeRangeFilter_ReturnsCorrectData() {
        // Given
        Map<String, Object> criteria = Map.of(
                "minAge", 18,
                "maxAge", 65,
                "from", "2024-01-01",
                "to", "2024-01-31"
        );

        when(visitRepository.count(any(Specification.class))).thenReturn(30L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(30L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    // Edge Cases
    @Test
    void fetchData_EmptyCriteria_ReturnsAllData() {
        // Given
        Map<String, Object> emptyCriteria = Map.of();
        when(visitRepository.count(any(Specification.class))).thenReturn(500L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(emptyCriteria);

        // Then
        assertEquals(500L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
        assertEquals("N/A - N/A", result.get("reportPeriod"));
    }

    //Edge Cases(Null criteria handling)
    @Test
    void fetchData_NullCriteria_ThrowsException() {
        // Given
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            dataProvider.fetchData(null);
        });
    }

    @Test
    void fetchData_WithLocalDateTimeObjects_HandlesCorrectly() {
        // Given
        LocalDateTime fromDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime toDate = LocalDateTime.of(2024, 1, 31, 23, 59);
        
        Map<String, Object> criteria = Map.of(
                "from", fromDate,
                "to", toDate
        );

        when(visitRepository.count(any(Specification.class))).thenReturn(100L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(100L, result.get("totalVisits"));
        verify(visitRepository).count(any(Specification.class));
    }

    @Test
    void fetchData_WithDateStringFormat_HandlesCorrectly() {
        // Given
        Map<String, Object> criteria = Map.of(
                "from", "2024-01-01T00:00:00",
                "to", "2024-01-31T23:59:59"
        );

        when(visitRepository.count(any(Specification.class))).thenReturn(80L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(80L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithOnlyFromDate_HandlesCorrectly() {
        // Given
        Map<String, Object> criteria = Map.of(
                "from", "2024-01-01"
        );

        when(visitRepository.count(any(Specification.class))).thenReturn(40L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(40L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithOnlyToDate_HandlesCorrectly() {
        // Given
        Map<String, Object> criteria = Map.of(
                "to", "2024-01-31"
        );

        when(visitRepository.count(any(Specification.class))).thenReturn(60L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(60L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithNoVisits_ReturnsZeroCounts() {
        // Given
        Map<String, Object> criteria = Map.of(
                "from", "2024-01-01",
                "to", "2024-01-31"
        );

        when(visitRepository.count(any(Specification.class))).thenReturn(0L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(List.of());

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(0L, result.get("totalVisits"));
        assertEquals(0L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithSinglePatientMultipleVisits_ReturnsCorrectUniqueCount() {
        // Given
        Patient singlePatient = new Patient();
        singlePatient.setPatientId(1L);
        
        Visit visit1 = new Visit();
        visit1.setPatient(singlePatient);
        visit1.setVisitDate(LocalDateTime.now());
        
        Visit visit2 = new Visit();
        visit2.setPatient(singlePatient);
        visit2.setVisitDate(LocalDateTime.now());
        
        Visit visit3 = new Visit();
        visit3.setPatient(singlePatient);
        visit3.setVisitDate(LocalDateTime.now());
        
        List<Visit> singlePatientVisits = List.of(visit1, visit2, visit3);

        Map<String, Object> criteria = Map.of(
                "from", "2024-01-01",
                "to", "2024-01-31"
        );

        when(visitRepository.count(any(Specification.class))).thenReturn(3L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(singlePatientVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(3L, result.get("totalVisits"));
        assertEquals(1L, result.get("uniquePatients")); // Only 1 unique patient
    }

    // Error Cases(Repository exception handling)
    @Test
    void fetchData_RepositoryThrowsException_PropagatesException() {
        // Given
        Map<String, Object> criteria = Map.of("from", "2024-01-01", "to", "2024-01-31");
        when(visitRepository.count(any(Specification.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dataProvider.fetchData(criteria);
        });

        assertEquals("Database error", exception.getMessage());
    }

    @Test
    void fetchData_FindAllThrowsException_PropagatesException() {
        // Given
        Map<String, Object> criteria = Map.of("from", "2024-01-01", "to", "2024-01-31");
        when(visitRepository.count(any(Specification.class))).thenReturn(10L);
        when(visitRepository.findAll(any(Specification.class)))
                .thenThrow(new RuntimeException("Query execution failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dataProvider.fetchData(criteria);
        });

        assertEquals("Query execution failed", exception.getMessage());
    }

    @Test
    void fetchData_WithInvalidDateFormat_HandlesGracefully() {
        // Given
        Map<String, Object> criteria = Map.of(
                "from", "invalid-date",
                "to", "2024-01-31"
        );

        when(visitRepository.count(any(Specification.class))).thenReturn(0L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(List.of());

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(0L, result.get("totalVisits"));
        assertEquals(0L, result.get("uniquePatients"));
    }

    // Additional tests to improve coverage
    @Test
    void fetchData_WithOnlyFromDate_HandlesCorrectly_Additional() {
        // Given
        Map<String, Object> criteria = Map.of(
                "from", "2024-01-01"
        );

        when(visitRepository.count(any(Specification.class))).thenReturn(40L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(40L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
        assertEquals("2024-01-01 - N/A", result.get("reportPeriod"));
    }

    @Test
    void fetchData_WithOnlyToDate_HandlesCorrectly_Additional() {
        // Given
        Map<String, Object> criteria = Map.of(
                "to", "2024-01-31"
        );

        when(visitRepository.count(any(Specification.class))).thenReturn(60L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(60L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
        assertEquals("N/A - 2024-01-31", result.get("reportPeriod"));
    }

    @Test
    void fetchData_WithGenderFilter_ReturnsCorrectData() {
        // Given
        Map<String, Object> criteria = Map.of(
                "gender", "FEMALE",
                "from", "2024-01-01",
                "to", "2024-01-31"
        );

        when(visitRepository.count(any(Specification.class))).thenReturn(25L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(25L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithPatientCategoryFilter_ReturnsCorrectData() {
        // Given
        Map<String, Object> criteria = Map.of(
                "patientCategory", "INPATIENT",
                "from", "2024-01-01",
                "to", "2024-01-31"
        );

        when(visitRepository.count(any(Specification.class))).thenReturn(15L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(15L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithComplexFilters_ReturnsCorrectData() {
        // Given
        Map<String, Object> criteria = Map.of(
                "hospitalId", "H001",
                "serviceCategory", "EMERGENCY",
                "patientCategory", "OUTPATIENT",
                "gender", "MALE",
                "minAge", 25,
                "maxAge", 50,
                "from", "2024-01-01",
                "to", "2024-01-31"
        );

        when(visitRepository.count(any(Specification.class))).thenReturn(5L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(5L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithNullValuesInCriteria_HandlesGracefully() {
        // Given
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("hospitalId", null);
        criteria.put("serviceCategory", "OPD");
        criteria.put("gender", null);
        criteria.put("from", "2024-01-01");
        criteria.put("to", "2024-01-31");

        when(visitRepository.count(any(Specification.class))).thenReturn(30L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(30L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithEmptyStringValues_HandlesGracefully() {
        // Given
        Map<String, Object> criteria = Map.of(
                "hospitalId", "",
                "serviceCategory", "OPD",
                "from", "2024-01-01",
                "to", "2024-01-31"
        );

        when(visitRepository.count(any(Specification.class))).thenReturn(20L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(20L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithZeroAgeRange_HandlesCorrectly() {
        // Given
        Map<String, Object> criteria = Map.of(
                "minAge", 0,
                "maxAge", 0,
                "from", "2024-01-01",
                "to", "2024-01-31"
        );

        when(visitRepository.count(any(Specification.class))).thenReturn(2L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(2L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithNegativeAgeRange_HandlesCorrectly() {
        // Given
        Map<String, Object> criteria = Map.of(
                "minAge", -5,
                "maxAge", -1,
                "from", "2024-01-01",
                "to", "2024-01-31"
        );

        when(visitRepository.count(any(Specification.class))).thenReturn(0L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(List.of());

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(0L, result.get("totalVisits"));
        assertEquals(0L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithVeryLargeAgeRange_HandlesCorrectly() {
        // Given
        Map<String, Object> criteria = Map.of(
                "minAge", 0,
                "maxAge", 150,
                "from", "2024-01-01",
                "to", "2024-01-31"
        );

        when(visitRepository.count(any(Specification.class))).thenReturn(100L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(100L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithSameFromAndToDate_HandlesCorrectly() {
        // Given
        Map<String, Object> criteria = Map.of(
                "from", "2024-01-15",
                "to", "2024-01-15"
        );

        when(visitRepository.count(any(Specification.class))).thenReturn(10L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(10L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithFutureDates_HandlesCorrectly() {
        // Given
        Map<String, Object> criteria = Map.of(
                "from", "2030-01-01",
                "to", "2030-01-31"
        );

        when(visitRepository.count(any(Specification.class))).thenReturn(0L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(List.of());

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(0L, result.get("totalVisits"));
        assertEquals(0L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithPastDates_HandlesCorrectly() {
        // Given
        Map<String, Object> criteria = Map.of(
                "from", "2020-01-01",
                "to", "2020-01-31"
        );

        when(visitRepository.count(any(Specification.class))).thenReturn(50L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(50L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithMixedDataTypes_HandlesCorrectly() {
        // Given
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("hospitalId", 123); // Integer instead of String
        criteria.put("serviceCategory", "OPD");
        criteria.put("minAge", "25"); // String instead of Integer
        criteria.put("maxAge", 65);
        criteria.put("from", "2024-01-01");
        criteria.put("to", "2024-01-31");

        when(visitRepository.count(any(Specification.class))).thenReturn(15L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(15L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    // Additional comprehensive tests to improve coverage
    @Test
    void fetchData_WithOnlyFromDate_NoToDate_HandlesCorrectly() {
        // Given
        Map<String, Object> criteria = Map.of("from", "2024-01-01");

        when(visitRepository.count(any(Specification.class))).thenReturn(25L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(25L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
        assertEquals("2024-01-01 - N/A", result.get("reportPeriod"));
    }

    @Test
    void fetchData_WithOnlyToDate_NoFromDate_HandlesCorrectly() {
        // Given
        Map<String, Object> criteria = Map.of("to", "2024-01-31");

        when(visitRepository.count(any(Specification.class))).thenReturn(35L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(35L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
        assertEquals("N/A - 2024-01-31", result.get("reportPeriod"));
    }

    @Test
    void fetchData_WithOnlyMinAge_NoMaxAge_HandlesCorrectly() {
        // Given
        Map<String, Object> criteria = Map.of("minAge", 18);

        when(visitRepository.count(any(Specification.class))).thenReturn(45L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(45L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithOnlyMaxAge_NoMinAge_HandlesCorrectly() {
        // Given
        Map<String, Object> criteria = Map.of("maxAge", 65);

        when(visitRepository.count(any(Specification.class))).thenReturn(55L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(55L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithOnlyHospitalId_HandlesCorrectly() {
        // Given
        Map<String, Object> criteria = Map.of("hospitalId", "H001");

        when(visitRepository.count(any(Specification.class))).thenReturn(65L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(65L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithOnlyServiceCategory_HandlesCorrectly() {
        // Given
        Map<String, Object> criteria = Map.of("serviceCategory", "EMERGENCY");

        when(visitRepository.count(any(Specification.class))).thenReturn(75L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(75L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithOnlyPatientCategory_HandlesCorrectly() {
        // Given
        Map<String, Object> criteria = Map.of("patientCategory", "INPATIENT");

        when(visitRepository.count(any(Specification.class))).thenReturn(85L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(85L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithOnlyGender_HandlesCorrectly() {
        // Given
        Map<String, Object> criteria = Map.of("gender", "FEMALE");

        when(visitRepository.count(any(Specification.class))).thenReturn(95L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(95L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithInvalidDateFormat_HandlesGracefully_Additional() {
        // Given
        Map<String, Object> criteria = Map.of(
                "from", "invalid-date-format",
                "to", "2024-01-31"
        );

        when(visitRepository.count(any(Specification.class))).thenReturn(0L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(List.of());

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(0L, result.get("totalVisits"));
        assertEquals(0L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithMalformedDateString_HandlesGracefully() {
        // Given
        Map<String, Object> criteria = Map.of(
                "from", "2024-13-45", // Invalid date
                "to", "2024-01-31"
        );

        when(visitRepository.count(any(Specification.class))).thenReturn(0L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(List.of());

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(0L, result.get("totalVisits"));
        assertEquals(0L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithEmptyStringValues_HandlesCorrectly() {
        // Given
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("hospitalId", "");
        criteria.put("serviceCategory", "");
        criteria.put("patientCategory", "");
        criteria.put("gender", "");
        criteria.put("from", "2024-01-01");
        criteria.put("to", "2024-01-31");

        when(visitRepository.count(any(Specification.class))).thenReturn(10L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(10L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithWhitespaceOnlyValues_HandlesCorrectly() {
        // Given
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("hospitalId", "   ");
        criteria.put("serviceCategory", "\t\n");
        criteria.put("patientCategory", "  \t  ");
        criteria.put("gender", "   ");
        criteria.put("from", "2024-01-01");
        criteria.put("to", "2024-01-31");

        when(visitRepository.count(any(Specification.class))).thenReturn(5L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(5L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithVeryLargeAgeRange_HandlesCorrectly_Additional() {
        // Given
        Map<String, Object> criteria = Map.of(
                "minAge", Integer.MIN_VALUE,
                "maxAge", Integer.MAX_VALUE,
                "from", "2024-01-01",
                "to", "2024-01-31"
        );

        when(visitRepository.count(any(Specification.class))).thenReturn(200L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(200L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithBoundaryAgeValues_HandlesCorrectly() {
        // Given
        Map<String, Object> criteria = Map.of(
                "minAge", 0,
                "maxAge", 120,
                "from", "2024-01-01",
                "to", "2024-01-31"
        );

        when(visitRepository.count(any(Specification.class))).thenReturn(150L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(150L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithSpecialCharactersInFilters_HandlesCorrectly() {
        // Given
        Map<String, Object> criteria = Map.of(
                "hospitalId", "H001@#$%",
                "serviceCategory", "OPD-EMERGENCY",
                "patientCategory", "OUT_PATIENT",
                "gender", "MALE/FEMALE",
                "from", "2024-01-01",
                "to", "2024-01-31"
        );

        when(visitRepository.count(any(Specification.class))).thenReturn(25L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(25L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithUnicodeCharactersInFilters_HandlesCorrectly() {
        // Given
        Map<String, Object> criteria = Map.of(
                "hospitalId", "H001你好",
                "serviceCategory", "OPD世界",
                "patientCategory", "OUT_PATIENT🌍",
                "gender", "MALE/FEMALE",
                "from", "2024-01-01",
                "to", "2024-01-31"
        );

        when(visitRepository.count(any(Specification.class))).thenReturn(30L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(30L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithVeryLongStringValues_HandlesCorrectly() {
        // Given
        String longString = "A".repeat(1000);
        Map<String, Object> criteria = Map.of(
                "hospitalId", longString,
                "serviceCategory", longString,
                "patientCategory", longString,
                "gender", longString,
                "from", "2024-01-01",
                "to", "2024-01-31"
        );

        when(visitRepository.count(any(Specification.class))).thenReturn(40L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(40L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithBooleanValues_HandlesCorrectly() {
        // Given
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("hospitalId", true);
        criteria.put("serviceCategory", false);
        criteria.put("patientCategory", true);
        criteria.put("gender", false);
        criteria.put("from", "2024-01-01");
        criteria.put("to", "2024-01-31");

        when(visitRepository.count(any(Specification.class))).thenReturn(50L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(50L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithDoubleValues_HandlesCorrectly() {
        // Given
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("hospitalId", 123.45);
        criteria.put("serviceCategory", 67.89);
        criteria.put("patientCategory", 90.12);
        criteria.put("gender", 34.56);
        criteria.put("from", "2024-01-01");
        criteria.put("to", "2024-01-31");

        when(visitRepository.count(any(Specification.class))).thenReturn(60L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(60L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithListValues_HandlesCorrectly() {
        // Given
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("hospitalId", List.of("H001", "H002"));
        criteria.put("serviceCategory", List.of("OPD", "EMERGENCY"));
        criteria.put("patientCategory", List.of("OUTPATIENT", "INPATIENT"));
        criteria.put("gender", List.of("MALE", "FEMALE"));
        criteria.put("from", "2024-01-01");
        criteria.put("to", "2024-01-31");

        when(visitRepository.count(any(Specification.class))).thenReturn(70L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(70L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_WithMapValues_HandlesCorrectly() {
        // Given
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("hospitalId", Map.of("id", "H001"));
        criteria.put("serviceCategory", Map.of("category", "OPD"));
        criteria.put("patientCategory", Map.of("type", "OUTPATIENT"));
        criteria.put("gender", Map.of("sex", "MALE"));
        criteria.put("from", "2024-01-01");
        criteria.put("to", "2024-01-31");

        when(visitRepository.count(any(Specification.class))).thenReturn(80L);
        when(visitRepository.findAll(any(Specification.class))).thenReturn(mockVisits);

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(80L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    // Helper to force execution of the Specification's predicate body for coverage
    private void executeSpecificationBody(Specification<Visit> spec) {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<Visit> cq = mock(CriteriaQuery.class);
        Root<Visit> root = mock(Root.class);

        // Path for all string-based fields
        @SuppressWarnings("unchecked")
        Path<Object> anyFieldPath = (Path<Object>) mock(Path.class);
        when(root.get(anyString())).thenReturn(anyFieldPath);

        Predicate predicate = mock(Predicate.class);
        org.mockito.Mockito.lenient().when(cb.equal(any(), any())).thenReturn(predicate);
        org.mockito.Mockito.lenient().when(cb.between(any(Path.class), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        // Execute the body
        spec.toPredicate(root, cq, cb);
    }

    @Test
    void fetchData_SpecificationBuildsPredicates_ForAllFilters() {
        // Given: include every supported filter to cover all branches inside the spec
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("hospitalId", "H001");
        criteria.put("serviceCategory", "OPD");
        criteria.put("patientCategory", "OUTPATIENT");
        criteria.put("gender", "MALE");
        criteria.put("minAge", 18);
        criteria.put("maxAge", 65);
        criteria.put("from", "2024-01-01");
        criteria.put("to", "2024-01-31");

        when(visitRepository.count(any(Specification.class))).thenAnswer(inv -> {
            Specification<Visit> spec = inv.getArgument(0);
            executeSpecificationBody(spec);
            return 99L;
        });
        when(visitRepository.findAll(any(Specification.class))).thenAnswer(inv -> {
            Specification<Visit> spec = inv.getArgument(0);
            executeSpecificationBody(spec);
            return mockVisits;
        });

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(99L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }

    @Test
    void fetchData_SpecificationParsesLocalDateTimeInputs() {
        // Given: from/to provided as LocalDateTime instances
        LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 1, 31, 23, 59, 59);
        Map<String, Object> criteria = Map.of("from", from, "to", to);

        when(visitRepository.count(any(Specification.class))).thenAnswer(inv -> {
            Specification<Visit> spec = inv.getArgument(0);
            executeSpecificationBody(spec);
            return 7L;
        });
        when(visitRepository.findAll(any(Specification.class))).thenAnswer(inv -> {
            Specification<Visit> spec = inv.getArgument(0);
            executeSpecificationBody(spec);
            return mockVisits;
        });

        // When
        Map<String, Object> result = dataProvider.fetchData(criteria);

        // Then
        assertEquals(7L, result.get("totalVisits"));
        assertEquals(2L, result.get("uniquePatients"));
    }
}