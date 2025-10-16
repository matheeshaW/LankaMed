package com.lankamed.health.backend.service;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import com.lankamed.health.backend.repository.VisitRepository;

/**
 * Unit tests for PatientVisitDataProvider.
 * - Mocks VisitRepository (no DB needed)
 * - Verifies correct behaviour for total count and filters
 */
@ExtendWith(MockitoExtension.class)
class PatientVisitDataProviderTest {

    @Mock
    private VisitRepository visitRepository;

    @InjectMocks
    private PatientVisitDataProvider provider;

    @BeforeEach
    void setup() {
        // No-op; mocks auto-injected
    }

    /**
     * Test with no filters set; should return simple mocked totalVisits.
     */
    @Test
    void testFetchData_noFilters() {
        when(visitRepository.count(any(Specification.class))).thenReturn(42L);
        Map<String, Object> criteria = new HashMap<>();
        Map<String, Object> data = provider.fetchData(criteria);
        assertEquals(42L, data.get("totalVisits"));
    }

    /**
     * Test with hospitalId and serviceCategory filters; repository mock returns filtered count.
     */
    @Test
    void testFetchData_withHospitalAndServiceCategory() {
        when(visitRepository.count(any(Specification.class))).thenReturn(7L);
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("hospitalId", "H001");
        criteria.put("serviceCategory", "General Medicine");
        Map<String, Object> data = provider.fetchData(criteria);
        assertEquals(7L, data.get("totalVisits"));
    }
}
