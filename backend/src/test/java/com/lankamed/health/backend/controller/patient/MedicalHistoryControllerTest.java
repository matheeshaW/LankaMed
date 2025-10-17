package com.lankamed.health.backend.controller.patient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lankamed.health.backend.dto.patient.*;
import com.lankamed.health.backend.model.patient.Allergy;
import com.lankamed.health.backend.service.patient.MedicalHistoryService;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MedicalHistoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class MedicalHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MedicalHistoryService medicalHistoryService;

    // Mock security-related beans that are picked up by the application context
    @MockBean
    private com.lankamed.health.backend.security.JwtUtil jwtUtil;

    @MockBean
    private com.lankamed.health.backend.service.CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("GET /api/patients/me/conditions")
    class GetMedicalConditions {
        @Test
        @WithMockUser(username = "patient@example.com")
        void getMedicalConditions_Success() throws Exception {
            List<MedicalConditionDto> conditions = Arrays.asList(
                    MedicalConditionDto.builder()
                            .conditionId(1L)
                            .conditionName("Diabetes")
                            .diagnosedDate(LocalDate.of(2020, 1, 1))
                            .notes("Active")
                            .notes("Type 2 diabetes")
                            .build(),
                    MedicalConditionDto.builder()
                            .conditionId(2L)
                            .conditionName("Hypertension")
                            .diagnosedDate(LocalDate.of(2019, 6, 15))
                            .notes("Controlled")
                            .notes("Well controlled with medication")
                            .build()
            );

            when(medicalHistoryService.getMedicalConditions()).thenReturn(conditions);

            mockMvc.perform(get("/api/patients/me/conditions")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].conditionId").value(1))
                    .andExpect(jsonPath("$[0].conditionName").value("Diabetes"))
                    .andExpect(jsonPath("$[0].notes").value("Type 2 diabetes"))
                    .andExpect(jsonPath("$[1].conditionId").value(2))
                    .andExpect(jsonPath("$[1].conditionName").value("Hypertension"))
                    .andExpect(jsonPath("$[1].notes").value("Well controlled with medication"));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void getMedicalConditions_EmptyList() throws Exception {
            when(medicalHistoryService.getMedicalConditions()).thenReturn(Arrays.asList());

            mockMvc.perform(get("/api/patients/me/conditions")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("POST /api/patients/me/conditions")
    class CreateMedicalCondition {
        @Test
        @WithMockUser(username = "patient@example.com")
        void createMedicalCondition_Success() throws Exception {
            CreateMedicalConditionDto createDto = new CreateMedicalConditionDto();
            createDto.setConditionName("Diabetes");
            createDto.setDiagnosedDate(LocalDate.of(2020, 1, 1));
            createDto.setNotes("Active");
            createDto.setNotes("Type 2 diabetes");

            MedicalConditionDto createdCondition = MedicalConditionDto.builder()
                    .conditionId(1L)
                    .conditionName("Diabetes")
                    .diagnosedDate(LocalDate.of(2020, 1, 1))
                    .notes("Active")
                    .notes("Type 2 diabetes")
                    .build();

            when(medicalHistoryService.createMedicalCondition(any(CreateMedicalConditionDto.class)))
                    .thenReturn(createdCondition);

            mockMvc.perform(post("/api/patients/me/conditions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.conditionId").value(1))
                    .andExpect(jsonPath("$.conditionName").value("Diabetes"))
                    .andExpect(jsonPath("$.notes").value("Type 2 diabetes"))
                    .andExpect(jsonPath("$.notes").value("Type 2 diabetes"));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void createMedicalCondition_InvalidData() throws Exception {
            CreateMedicalConditionDto createDto = new CreateMedicalConditionDto();
            createDto.setConditionName(""); // Invalid empty name
            createDto.setNotes("");

            mockMvc.perform(post("/api/patients/me/conditions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/patients/me/conditions/{conditionId}")
    class UpdateMedicalCondition {
        @Test
        @WithMockUser(username = "patient@example.com")
        void updateMedicalCondition_Success() throws Exception {
            Long conditionId = 1L;
            CreateMedicalConditionDto updateDto = new CreateMedicalConditionDto();
            updateDto.setConditionName("Diabetes Type 2");
            updateDto.setNotes("Controlled");
            updateDto.setNotes("Well controlled with medication");

            MedicalConditionDto updatedCondition = MedicalConditionDto.builder()
                    .conditionId(conditionId)
                    .conditionName("Diabetes Type 2")
                    .notes("Controlled")
                    .notes("Well controlled with medication")
                    .build();

            when(medicalHistoryService.updateMedicalCondition(eq(conditionId), any(CreateMedicalConditionDto.class)))
                    .thenReturn(updatedCondition);

            mockMvc.perform(put("/api/patients/me/conditions/{conditionId}", conditionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.conditionId").value(conditionId))
                    .andExpect(jsonPath("$.conditionName").value("Diabetes Type 2"))
                    .andExpect(jsonPath("$.notes").value("Well controlled with medication"));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void updateMedicalCondition_InvalidData() throws Exception {
            Long conditionId = 1L;
            CreateMedicalConditionDto updateDto = new CreateMedicalConditionDto();
            updateDto.setConditionName(""); // Invalid empty name
            updateDto.setNotes("");

            mockMvc.perform(put("/api/patients/me/conditions/{conditionId}", conditionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/patients/me/conditions/{conditionId}")
    class DeleteMedicalCondition {
        @Test
        @WithMockUser(username = "patient@example.com")
        void deleteMedicalCondition_Success() throws Exception {
            Long conditionId = 1L;

            mockMvc.perform(delete("/api/patients/me/conditions/{conditionId}", conditionId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("GET /api/patients/me/allergies")
    class GetAllergies {
        @Test
        @WithMockUser(username = "patient@example.com")
        void getAllergies_Success() throws Exception {
            List<AllergyDto> allergies = Arrays.asList(
                    AllergyDto.builder()
                            .allergyId(1L)
                            .allergyName("Penicillin")
                            .severity(Allergy.Severity.SEVERE)
                            .notes("Rash and difficulty breathing")
                            .build(),
                    AllergyDto.builder()
                            .allergyId(2L)
                            .allergyName("Shellfish")
                            .severity(Allergy.Severity.MODERATE)
                            .notes("Nausea and vomiting")
                            .build()
            );

            when(medicalHistoryService.getAllergies()).thenReturn(allergies);

            mockMvc.perform(get("/api/patients/me/allergies")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].allergyId").value(1))
                    .andExpect(jsonPath("$[0].allergyName").value("Penicillin"))
                    .andExpect(jsonPath("$[0].severity").value("SEVERE"))
                    .andExpect(jsonPath("$[1].allergyId").value(2))
                    .andExpect(jsonPath("$[1].allergyName").value("Shellfish"))
                    .andExpect(jsonPath("$[1].severity").value("MODERATE"));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void getAllergies_EmptyList() throws Exception {
            when(medicalHistoryService.getAllergies()).thenReturn(Arrays.asList());

            mockMvc.perform(get("/api/patients/me/allergies")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("POST /api/patients/me/allergies")
    class CreateAllergy {
        @Test
        @WithMockUser(username = "patient@example.com")
        void createAllergy_Success() throws Exception {
            CreateAllergyDto createDto = new CreateAllergyDto();
            createDto.setAllergyName("Penicillin");
            createDto.setSeverity(Allergy.Severity.SEVERE);
            createDto.setNotes("Rash and difficulty breathing");

            AllergyDto createdAllergy = AllergyDto.builder()
                    .allergyId(1L)
                    .allergyName("Penicillin")
                    .severity(Allergy.Severity.SEVERE)
                    .notes("Rash and difficulty breathing")
                    .build();

            when(medicalHistoryService.createAllergy(any(CreateAllergyDto.class)))
                    .thenReturn(createdAllergy);

            mockMvc.perform(post("/api/patients/me/allergies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.allergyId").value(1))
                    .andExpect(jsonPath("$.allergyName").value("Penicillin"))
                    .andExpect(jsonPath("$.severity").value("SEVERE"))
                    .andExpect(jsonPath("$.notes").value("Rash and difficulty breathing"));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void createAllergy_InvalidData() throws Exception {
            CreateAllergyDto createDto = new CreateAllergyDto();
            createDto.setAllergyName(""); // Invalid empty name
            createDto.setSeverity(null); // Invalid null severity

            mockMvc.perform(post("/api/patients/me/allergies")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/patients/me/allergies/{allergyId}")
    class UpdateAllergy {
        @Test
        @WithMockUser(username = "patient@example.com")
        void updateAllergy_Success() throws Exception {
            Long allergyId = 1L;
            CreateAllergyDto updateDto = new CreateAllergyDto();
            updateDto.setAllergyName("Penicillin");
            updateDto.setSeverity(Allergy.Severity.MODERATE);
            updateDto.setNotes("Mild rash only");

            AllergyDto updatedAllergy = AllergyDto.builder()
                    .allergyId(allergyId)
                    .allergyName("Penicillin")
                    .severity(Allergy.Severity.MODERATE)
                    .notes("Mild rash only")
                    .build();

            when(medicalHistoryService.updateAllergy(eq(allergyId), any(CreateAllergyDto.class)))
                    .thenReturn(updatedAllergy);

            mockMvc.perform(put("/api/patients/me/allergies/{allergyId}", allergyId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.allergyId").value(allergyId))
                    .andExpect(jsonPath("$.allergyName").value("Penicillin"))
                    .andExpect(jsonPath("$.severity").value("MODERATE"))
                    .andExpect(jsonPath("$.notes").value("Mild rash only"));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void updateAllergy_InvalidData() throws Exception {
            Long allergyId = 1L;
            CreateAllergyDto updateDto = new CreateAllergyDto();
            updateDto.setAllergyName(""); // Invalid empty name
            updateDto.setSeverity(null); // Invalid null severity

            mockMvc.perform(put("/api/patients/me/allergies/{allergyId}", allergyId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/patients/me/allergies/{allergyId}")
    class DeleteAllergy {
        @Test
        @WithMockUser(username = "patient@example.com")
        void deleteAllergy_Success() throws Exception {
            Long allergyId = 1L;

            mockMvc.perform(delete("/api/patients/me/allergies/{allergyId}", allergyId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("GET /api/patients/me/prescriptions")
    class GetPrescriptions {
        @Test
        @WithMockUser(username = "patient@example.com")
        void getPrescriptions_Success() throws Exception {
            List<PrescriptionDto> prescriptions = Arrays.asList(
                    PrescriptionDto.builder()
                            .prescriptionId(1L)
                            .medicationName("Metformin")
                            .dosage("500mg")
                            .frequency("Twice daily")
                            .startDate(LocalDate.of(2020, 1, 1))
                            .endDate(LocalDate.of(2020, 12, 31))
                            .doctorName("Dr. Smith")
                            .doctorSpecialization("Endocrinology")
                            .build(),
                    PrescriptionDto.builder()
                            .prescriptionId(2L)
                            .medicationName("Lisinopril")
                            .dosage("10mg")
                            .frequency("Once daily")
                            .startDate(LocalDate.of(2019, 6, 1))
                            .endDate(LocalDate.of(2020, 6, 1))
                            .doctorName("Dr. Johnson")
                            .doctorSpecialization("Cardiology")
                            .build()
            );

            when(medicalHistoryService.getPrescriptions()).thenReturn(prescriptions);

            mockMvc.perform(get("/api/patients/me/prescriptions")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].prescriptionId").value(1))
                    .andExpect(jsonPath("$[0].medicationName").value("Metformin"))
                    .andExpect(jsonPath("$[0].dosage").value("500mg"))
                    .andExpect(jsonPath("$[0].doctorName").value("Dr. Smith"))
                    .andExpect(jsonPath("$[1].prescriptionId").value(2))
                    .andExpect(jsonPath("$[1].medicationName").value("Lisinopril"))
                    .andExpect(jsonPath("$[1].dosage").value("10mg"))
                    .andExpect(jsonPath("$[1].doctorName").value("Dr. Johnson"));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void getPrescriptions_EmptyList() throws Exception {
            when(medicalHistoryService.getPrescriptions()).thenReturn(Arrays.asList());

            mockMvc.perform(get("/api/patients/me/prescriptions")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }
}
