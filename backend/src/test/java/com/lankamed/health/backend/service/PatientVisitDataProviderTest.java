package com.lankamed.health.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import com.lankamed.health.backend.model.Visit;
import com.lankamed.health.backend.model.patient.Patient;
import com.lankamed.health.backend.repository.VisitRepository;

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

    // Positive Test Cases
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

    // Error Cases
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
}