package com.lankamed.health.backend.controller.patient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lankamed.health.backend.dto.patient.WeightRecordDto;
import com.lankamed.health.backend.service.patient.WeightRecordService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WeightHistoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class WeightHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeightRecordService weightService;

    // Mock security-related beans that are picked up by the application context
    @MockBean
    private com.lankamed.health.backend.security.JwtUtil jwtUtil;

    @MockBean
    private com.lankamed.health.backend.service.CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("GET /api/patients/me/weight-records")
    class GetAllRecords {
        @Test
        @WithMockUser(username = "patient@example.com")
        void getAllRecords_Success() throws Exception {
            LocalDateTime now = LocalDateTime.now();
            List<WeightRecordDto> records = Arrays.asList(
                    WeightRecordDto.builder()
                            .weightKg(70.5)
                            .timestamp(now)
                            .build(),
                    WeightRecordDto.builder()
                            .weightKg(69.8)
                            .timestamp(now.minusDays(1))
                            .build(),
                    WeightRecordDto.builder()
                            .weightKg(71.2)
                            .timestamp(now.minusDays(2))
                            .build()
            );

            when(weightService.getAllRecordsForCurrentPatient()).thenReturn(records);

            mockMvc.perform(get("/api/patients/me/weight-records")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0].weightKg").value(70.5))
                    .andExpect(jsonPath("$[1].weightKg").value(69.8))
                    .andExpect(jsonPath("$[2].weightKg").value(71.2));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void getAllRecords_EmptyList() throws Exception {
            when(weightService.getAllRecordsForCurrentPatient()).thenReturn(Arrays.asList());

            mockMvc.perform(get("/api/patients/me/weight-records")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void getAllRecords_SingleRecord() throws Exception {
            LocalDateTime now = LocalDateTime.now();
            List<WeightRecordDto> records = Arrays.asList(
                    WeightRecordDto.builder()
                            .weightKg(75.2)
                            .timestamp(now)
                            .build()
            );

            when(weightService.getAllRecordsForCurrentPatient()).thenReturn(records);

            mockMvc.perform(get("/api/patients/me/weight-records")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].weightKg").value(75.2));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void getAllRecords_WithSortedTimestamps() throws Exception {
            LocalDateTime baseTime = LocalDateTime.of(2023, 1, 1, 12, 0);
            List<WeightRecordDto> records = Arrays.asList(
                    WeightRecordDto.builder()
                            .weightKg(70.0)
                            .timestamp(baseTime.plusDays(2)) // Latest
                            .build(),
                    WeightRecordDto.builder()
                            .weightKg(69.5)
                            .timestamp(baseTime) // Earliest
                            .build(),
                    WeightRecordDto.builder()
                            .weightKg(69.8)
                            .timestamp(baseTime.plusDays(1)) // Middle
                            .build()
            );

            when(weightService.getAllRecordsForCurrentPatient()).thenReturn(records);

            mockMvc.perform(get("/api/patients/me/weight-records")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0].weightKg").value(70.0))
                    .andExpect(jsonPath("$[1].weightKg").value(69.5))
                    .andExpect(jsonPath("$[2].weightKg").value(69.8));
        }
    }

    @Nested
    @DisplayName("POST /api/patients/me/weight-records")
    class AddRecord {
        @Test
        @WithMockUser(username = "patient@example.com")
        void addRecord_Success() throws Exception {
            LocalDateTime now = LocalDateTime.now();
            WeightRecordDto inputDto = WeightRecordDto.builder()
                    .weightKg(70.5)
                    .timestamp(now)
                    .build();

            WeightRecordDto savedRecord = WeightRecordDto.builder()
                    .weightKg(70.5)
                    .timestamp(now)
                    .build();

            when(weightService.addRecord(any(WeightRecordDto.class))).thenReturn(savedRecord);

            mockMvc.perform(post("/api/patients/me/weight-records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.weightKg").value(70.5));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void addRecord_WithZeroWeight() throws Exception {
            LocalDateTime now = LocalDateTime.now();
            WeightRecordDto inputDto = WeightRecordDto.builder()
                    .weightKg(0.0)
                    .timestamp(now)
                    .build();

            WeightRecordDto savedRecord = WeightRecordDto.builder()
                    .weightKg(0.0)
                    .timestamp(now)
                    .build();

            when(weightService.addRecord(any(WeightRecordDto.class))).thenReturn(savedRecord);

            mockMvc.perform(post("/api/patients/me/weight-records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.weightKg").value(0.0));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void addRecord_WithHighWeight() throws Exception {
            LocalDateTime now = LocalDateTime.now();
            WeightRecordDto inputDto = WeightRecordDto.builder()
                    .weightKg(200.5)
                    .timestamp(now)
                    .build();

            WeightRecordDto savedRecord = WeightRecordDto.builder()
                    .weightKg(200.5)
                    .timestamp(now)
                    .build();

            when(weightService.addRecord(any(WeightRecordDto.class))).thenReturn(savedRecord);

            mockMvc.perform(post("/api/patients/me/weight-records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.weightKg").value(200.5));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void addRecord_WithDecimalWeight() throws Exception {
            LocalDateTime now = LocalDateTime.now();
            WeightRecordDto inputDto = WeightRecordDto.builder()
                    .weightKg(68.75)
                    .timestamp(now)
                    .build();

            WeightRecordDto savedRecord = WeightRecordDto.builder()
                    .weightKg(68.75)
                    .timestamp(now)
                    .build();

            when(weightService.addRecord(any(WeightRecordDto.class))).thenReturn(savedRecord);

            mockMvc.perform(post("/api/patients/me/weight-records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.weightKg").value(68.75));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void addRecord_WithSpecificTimestamp() throws Exception {
            LocalDateTime specificTime = LocalDateTime.of(2023, 12, 25, 10, 30);
            WeightRecordDto inputDto = WeightRecordDto.builder()
                    .weightKg(68.3)
                    .timestamp(specificTime)
                    .build();

            WeightRecordDto savedRecord = WeightRecordDto.builder()
                    .weightKg(68.3)
                    .timestamp(specificTime)
                    .build();

            when(weightService.addRecord(any(WeightRecordDto.class))).thenReturn(savedRecord);

            mockMvc.perform(post("/api/patients/me/weight-records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.weightKg").value(68.3));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void addRecord_WithVerySmallWeight() throws Exception {
            LocalDateTime now = LocalDateTime.now();
            WeightRecordDto inputDto = WeightRecordDto.builder()
                    .weightKg(0.1)
                    .timestamp(now)
                    .build();

            WeightRecordDto savedRecord = WeightRecordDto.builder()
                    .weightKg(0.1)
                    .timestamp(now)
                    .build();

            when(weightService.addRecord(any(WeightRecordDto.class))).thenReturn(savedRecord);

            mockMvc.perform(post("/api/patients/me/weight-records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.weightKg").value(0.1));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void addRecord_WithVeryLargeWeight() throws Exception {
            LocalDateTime now = LocalDateTime.now();
            WeightRecordDto inputDto = WeightRecordDto.builder()
                    .weightKg(500.0)
                    .timestamp(now)
                    .build();

            WeightRecordDto savedRecord = WeightRecordDto.builder()
                    .weightKg(500.0)
                    .timestamp(now)
                    .build();

            when(weightService.addRecord(any(WeightRecordDto.class))).thenReturn(savedRecord);

            mockMvc.perform(post("/api/patients/me/weight-records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.weightKg").value(500.0));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void addRecord_InvalidJson() throws Exception {
            String invalidJson = "{ invalid json }";

            mockMvc.perform(post("/api/patients/me/weight-records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void addRecord_EmptyRequestBody() throws Exception {
            mockMvc.perform(post("/api/patients/me/weight-records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void addRecord_NullRequestBody() throws Exception {
            mockMvc.perform(post("/api/patients/me/weight-records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("null"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void addRecord_WithNegativeWeight() throws Exception {
            LocalDateTime now = LocalDateTime.now();
            WeightRecordDto inputDto = WeightRecordDto.builder()
                    .weightKg(-10.0) // Negative weight (edge case)
                    .timestamp(now)
                    .build();

            WeightRecordDto savedRecord = WeightRecordDto.builder()
                    .weightKg(-10.0)
                    .timestamp(now)
                    .build();

            when(weightService.addRecord(any(WeightRecordDto.class))).thenReturn(savedRecord);

            mockMvc.perform(post("/api/patients/me/weight-records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.weightKg").value(-10.0));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void addRecord_WithPreciseDecimalWeight() throws Exception {
            LocalDateTime now = LocalDateTime.now();
            WeightRecordDto inputDto = WeightRecordDto.builder()
                    .weightKg(70.123456789) // Very precise decimal
                    .timestamp(now)
                    .build();

            WeightRecordDto savedRecord = WeightRecordDto.builder()
                    .weightKg(70.123456789)
                    .timestamp(now)
                    .build();

            when(weightService.addRecord(any(WeightRecordDto.class))).thenReturn(savedRecord);

            mockMvc.perform(post("/api/patients/me/weight-records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.weightKg").value(70.123456789));
        }
    }
}
