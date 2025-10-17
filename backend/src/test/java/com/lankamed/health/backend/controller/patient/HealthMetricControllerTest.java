package com.lankamed.health.backend.controller.patient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lankamed.health.backend.dto.patient.HealthMetricDto;
import com.lankamed.health.backend.service.patient.HealthMetricService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HealthMetricController.class)
@AutoConfigureMockMvc(addFilters = false)
class HealthMetricControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HealthMetricService healthMetricService;

    // Mock security-related beans that are picked up by the application context
    @MockBean
    private com.lankamed.health.backend.security.JwtUtil jwtUtil;

    @MockBean
    private com.lankamed.health.backend.service.CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("GET /api/patients/me/health-metrics")
    class GetAllMetrics {
        @Test
        @WithMockUser(username = "patient@example.com")
        void getAllMetrics_Success() throws Exception {
            LocalDateTime now = LocalDateTime.now();
            List<HealthMetricDto> metrics = Arrays.asList(
                    HealthMetricDto.builder()
                            .systolic(120)
                            .diastolic(80)
                            .heartRate(72)
                            .spo2(98)
                            .timestamp(now)
                            .build(),
                    HealthMetricDto.builder()
                            .systolic(115)
                            .diastolic(75)
                            .heartRate(68)
                            .spo2(99)
                            .timestamp(now.minusHours(1))
                            .build()
            );

            when(healthMetricService.getMetricsForCurrentPatient()).thenReturn(metrics);

            mockMvc.perform(get("/api/patients/me/health-metrics")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].systolic").value(120))
                    .andExpect(jsonPath("$[0].diastolic").value(80))
                    .andExpect(jsonPath("$[0].heartRate").value(72))
                    .andExpect(jsonPath("$[0].spo2").value(98))
                    .andExpect(jsonPath("$[1].systolic").value(115))
                    .andExpect(jsonPath("$[1].diastolic").value(75))
                    .andExpect(jsonPath("$[1].heartRate").value(68))
                    .andExpect(jsonPath("$[1].spo2").value(99));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void getAllMetrics_EmptyList() throws Exception {
            when(healthMetricService.getMetricsForCurrentPatient()).thenReturn(Arrays.asList());

            mockMvc.perform(get("/api/patients/me/health-metrics")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/patients/me/health-metrics/latest")
    class GetLatestMetric {
        @Test
        @WithMockUser(username = "patient@example.com")
        void getLatestMetric_Success() throws Exception {
            LocalDateTime now = LocalDateTime.now();
            HealthMetricDto latestMetric = HealthMetricDto.builder()
                    .systolic(125)
                    .diastolic(85)
                    .heartRate(75)
                    .spo2(96)
                    .timestamp(now)
                    .build();

            when(healthMetricService.getLatestMetric()).thenReturn(Optional.of(latestMetric));

            mockMvc.perform(get("/api/patients/me/health-metrics/latest")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.systolic").value(125))
                    .andExpect(jsonPath("$.diastolic").value(85))
                    .andExpect(jsonPath("$.heartRate").value(75))
                    .andExpect(jsonPath("$.spo2").value(96));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void getLatestMetric_NoMetricsFound() throws Exception {
            when(healthMetricService.getLatestMetric()).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/patients/me/health-metrics/latest")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("POST /api/patients/me/health-metrics")
    class AddMetric {
        @Test
        @WithMockUser(username = "patient@example.com")
        void addMetric_Success() throws Exception {
            LocalDateTime now = LocalDateTime.now();
            HealthMetricDto inputDto = HealthMetricDto.builder()
                    .systolic(120)
                    .diastolic(80)
                    .heartRate(72)
                    .spo2(98)
                    .timestamp(now)
                    .build();

            HealthMetricDto savedMetric = HealthMetricDto.builder()
                    .systolic(120)
                    .diastolic(80)
                    .heartRate(72)
                    .spo2(98)
                    .timestamp(now)
                    .build();

            when(healthMetricService.addMetric(any(HealthMetricDto.class))).thenReturn(savedMetric);

            mockMvc.perform(post("/api/patients/me/health-metrics")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.systolic").value(120))
                    .andExpect(jsonPath("$.diastolic").value(80))
                    .andExpect(jsonPath("$.heartRate").value(72))
                    .andExpect(jsonPath("$.spo2").value(98));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void addMetric_WithMinimalValues() throws Exception {
            LocalDateTime now = LocalDateTime.now();
            HealthMetricDto inputDto = HealthMetricDto.builder()
                    .systolic(90)
                    .diastolic(60)
                    .heartRate(50)
                    .spo2(90)
                    .timestamp(now)
                    .build();

            HealthMetricDto savedMetric = HealthMetricDto.builder()
                    .systolic(90)
                    .diastolic(60)
                    .heartRate(50)
                    .spo2(90)
                    .timestamp(now)
                    .build();

            when(healthMetricService.addMetric(any(HealthMetricDto.class))).thenReturn(savedMetric);

            mockMvc.perform(post("/api/patients/me/health-metrics")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.systolic").value(90))
                    .andExpect(jsonPath("$.diastolic").value(60))
                    .andExpect(jsonPath("$.heartRate").value(50))
                    .andExpect(jsonPath("$.spo2").value(90));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void addMetric_WithHighValues() throws Exception {
            LocalDateTime now = LocalDateTime.now();
            HealthMetricDto inputDto = HealthMetricDto.builder()
                    .systolic(180)
                    .diastolic(120)
                    .heartRate(120)
                    .spo2(100)
                    .timestamp(now)
                    .build();

            HealthMetricDto savedMetric = HealthMetricDto.builder()
                    .systolic(180)
                    .diastolic(120)
                    .heartRate(120)
                    .spo2(100)
                    .timestamp(now)
                    .build();

            when(healthMetricService.addMetric(any(HealthMetricDto.class))).thenReturn(savedMetric);

            mockMvc.perform(post("/api/patients/me/health-metrics")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.systolic").value(180))
                    .andExpect(jsonPath("$.diastolic").value(120))
                    .andExpect(jsonPath("$.heartRate").value(120))
                    .andExpect(jsonPath("$.spo2").value(100));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void addMetric_InvalidJson() throws Exception {
            String invalidJson = "{ invalid json }";

            mockMvc.perform(post("/api/patients/me/health-metrics")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void addMetric_EmptyRequestBody() throws Exception {
            mockMvc.perform(post("/api/patients/me/health-metrics")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void addMetric_NullRequestBody() throws Exception {
            mockMvc.perform(post("/api/patients/me/health-metrics")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("null"))
                    .andExpect(status().isBadRequest());
        }
    }
}
