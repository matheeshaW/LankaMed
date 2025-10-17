package com.lankamed.health.backend.controller.patient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lankamed.health.backend.dto.patient.BloodPressureRecordDto;
import com.lankamed.health.backend.service.patient.BloodPressureRecordService;
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

@WebMvcTest(BloodPressureTrendController.class)
@AutoConfigureMockMvc(addFilters = false)
class BloodPressureTrendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BloodPressureRecordService bpService;

    // Mock security-related beans that are picked up by the application context
    @MockBean
    private com.lankamed.health.backend.security.JwtUtil jwtUtil;

    @MockBean
    private com.lankamed.health.backend.service.CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("GET /api/patients/me/blood-pressure-records")
    class GetAllRecords {
        @Test
        @WithMockUser(username = "patient@example.com")
        void getAllRecords_Success() throws Exception {
            LocalDateTime now = LocalDateTime.now();
            List<BloodPressureRecordDto> records = Arrays.asList(
                    BloodPressureRecordDto.builder()
                            .systolic(120)
                            .diastolic(80)
                            .timestamp(now)
                            .build(),
                    BloodPressureRecordDto.builder()
                            .systolic(115)
                            .diastolic(75)
                            .timestamp(now.minusHours(2))
                            .build(),
                    BloodPressureRecordDto.builder()
                            .systolic(125)
                            .diastolic(85)
                            .timestamp(now.minusHours(4))
                            .build()
            );

            when(bpService.getAllRecordsForCurrentPatient()).thenReturn(records);

            mockMvc.perform(get("/api/patients/me/blood-pressure-records")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0].systolic").value(120))
                    .andExpect(jsonPath("$[0].diastolic").value(80))
                    .andExpect(jsonPath("$[1].systolic").value(115))
                    .andExpect(jsonPath("$[1].diastolic").value(75))
                    .andExpect(jsonPath("$[2].systolic").value(125))
                    .andExpect(jsonPath("$[2].diastolic").value(85));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void getAllRecords_EmptyList() throws Exception {
            when(bpService.getAllRecordsForCurrentPatient()).thenReturn(Arrays.asList());

            mockMvc.perform(get("/api/patients/me/blood-pressure-records")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void getAllRecords_SingleRecord() throws Exception {
            LocalDateTime now = LocalDateTime.now();
            List<BloodPressureRecordDto> records = Arrays.asList(
                    BloodPressureRecordDto.builder()
                            .systolic(118)
                            .diastolic(78)
                            .timestamp(now)
                            .build()
            );

            when(bpService.getAllRecordsForCurrentPatient()).thenReturn(records);

            mockMvc.perform(get("/api/patients/me/blood-pressure-records")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].systolic").value(118))
                    .andExpect(jsonPath("$[0].diastolic").value(78));
        }
    }

    @Nested
    @DisplayName("POST /api/patients/me/blood-pressure-records")
    class AddRecord {
        @Test
        @WithMockUser(username = "patient@example.com")
        void addRecord_Success() throws Exception {
            LocalDateTime now = LocalDateTime.now();
            BloodPressureRecordDto inputDto = BloodPressureRecordDto.builder()
                    .systolic(120)
                    .diastolic(80)
                    .timestamp(now)
                    .build();

            BloodPressureRecordDto savedRecord = BloodPressureRecordDto.builder()
                    .systolic(120)
                    .diastolic(80)
                    .timestamp(now)
                    .build();

            when(bpService.addRecord(any(BloodPressureRecordDto.class))).thenReturn(savedRecord);

            mockMvc.perform(post("/api/patients/me/blood-pressure-records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.systolic").value(120))
                    .andExpect(jsonPath("$.diastolic").value(80));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void addRecord_WithLowBloodPressure() throws Exception {
            LocalDateTime now = LocalDateTime.now();
            BloodPressureRecordDto inputDto = BloodPressureRecordDto.builder()
                    .systolic(90)
                    .diastolic(60)
                    .timestamp(now)
                    .build();

            BloodPressureRecordDto savedRecord = BloodPressureRecordDto.builder()
                    .systolic(90)
                    .diastolic(60)
                    .timestamp(now)
                    .build();

            when(bpService.addRecord(any(BloodPressureRecordDto.class))).thenReturn(savedRecord);

            mockMvc.perform(post("/api/patients/me/blood-pressure-records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.systolic").value(90))
                    .andExpect(jsonPath("$.diastolic").value(60));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void addRecord_WithHighBloodPressure() throws Exception {
            LocalDateTime now = LocalDateTime.now();
            BloodPressureRecordDto inputDto = BloodPressureRecordDto.builder()
                    .systolic(180)
                    .diastolic(120)
                    .timestamp(now)
                    .build();

            BloodPressureRecordDto savedRecord = BloodPressureRecordDto.builder()
                    .systolic(180)
                    .diastolic(120)
                    .timestamp(now)
                    .build();

            when(bpService.addRecord(any(BloodPressureRecordDto.class))).thenReturn(savedRecord);

            mockMvc.perform(post("/api/patients/me/blood-pressure-records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.systolic").value(180))
                    .andExpect(jsonPath("$.diastolic").value(120));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void addRecord_WithNormalBloodPressure() throws Exception {
            LocalDateTime now = LocalDateTime.now();
            BloodPressureRecordDto inputDto = BloodPressureRecordDto.builder()
                    .systolic(110)
                    .diastolic(70)
                    .timestamp(now)
                    .build();

            BloodPressureRecordDto savedRecord = BloodPressureRecordDto.builder()
                    .systolic(110)
                    .diastolic(70)
                    .timestamp(now)
                    .build();

            when(bpService.addRecord(any(BloodPressureRecordDto.class))).thenReturn(savedRecord);

            mockMvc.perform(post("/api/patients/me/blood-pressure-records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.systolic").value(110))
                    .andExpect(jsonPath("$.diastolic").value(70));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void addRecord_WithSpecificTimestamp() throws Exception {
            LocalDateTime specificTime = LocalDateTime.of(2023, 12, 25, 14, 30);
            BloodPressureRecordDto inputDto = BloodPressureRecordDto.builder()
                    .systolic(118)
                    .diastolic(78)
                    .timestamp(specificTime)
                    .build();

            BloodPressureRecordDto savedRecord = BloodPressureRecordDto.builder()
                    .systolic(118)
                    .diastolic(78)
                    .timestamp(specificTime)
                    .build();

            when(bpService.addRecord(any(BloodPressureRecordDto.class))).thenReturn(savedRecord);

            mockMvc.perform(post("/api/patients/me/blood-pressure-records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.systolic").value(118))
                    .andExpect(jsonPath("$.diastolic").value(78));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void addRecord_InvalidJson() throws Exception {
            String invalidJson = "{ invalid json }";

            mockMvc.perform(post("/api/patients/me/blood-pressure-records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void addRecord_EmptyRequestBody() throws Exception {
            mockMvc.perform(post("/api/patients/me/blood-pressure-records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void addRecord_NullRequestBody() throws Exception {
            mockMvc.perform(post("/api/patients/me/blood-pressure-records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("null"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void addRecord_WithZeroValues() throws Exception {
            LocalDateTime now = LocalDateTime.now();
            BloodPressureRecordDto inputDto = BloodPressureRecordDto.builder()
                    .systolic(0)
                    .diastolic(0)
                    .timestamp(now)
                    .build();

            BloodPressureRecordDto savedRecord = BloodPressureRecordDto.builder()
                    .systolic(0)
                    .diastolic(0)
                    .timestamp(now)
                    .build();

            when(bpService.addRecord(any(BloodPressureRecordDto.class))).thenReturn(savedRecord);

            mockMvc.perform(post("/api/patients/me/blood-pressure-records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.systolic").value(0))
                    .andExpect(jsonPath("$.diastolic").value(0));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void addRecord_WithExtremeValues() throws Exception {
            LocalDateTime now = LocalDateTime.now();
            BloodPressureRecordDto inputDto = BloodPressureRecordDto.builder()
                    .systolic(300)
                    .diastolic(200)
                    .timestamp(now)
                    .build();

            BloodPressureRecordDto savedRecord = BloodPressureRecordDto.builder()
                    .systolic(300)
                    .diastolic(200)
                    .timestamp(now)
                    .build();

            when(bpService.addRecord(any(BloodPressureRecordDto.class))).thenReturn(savedRecord);

            mockMvc.perform(post("/api/patients/me/blood-pressure-records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.systolic").value(300))
                    .andExpect(jsonPath("$.diastolic").value(200));
        }
    }
}
