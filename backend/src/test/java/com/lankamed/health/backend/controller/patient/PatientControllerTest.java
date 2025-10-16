package com.lankamed.health.backend.controller.patient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lankamed.health.backend.dto.patient.PatientProfileDto;
import com.lankamed.health.backend.dto.patient.UpdatePatientProfileDto;
import com.lankamed.health.backend.model.patient.Patient;
import com.lankamed.health.backend.service.patient.PatientService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(PatientController.class)
@AutoConfigureMockMvc(addFilters = false)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientService patientService;

        // Mock security-related beans that are picked up by the application context (filters etc.)
        @MockBean
        private com.lankamed.health.backend.security.JwtUtil jwtUtil;

        @MockBean
        private com.lankamed.health.backend.service.CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("GET /api/patients/me")
    class GetProfile {
        @Test
        @WithMockUser(username = "john.doe@example.com")
        void returnsFullProfileWhenPatientExists() throws Exception {
            PatientProfileDto profileDto = PatientProfileDto.builder()
                    .patientId(1L)
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@example.com")
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .gender(Patient.Gender.MALE)
                    .contactNumber("+1234567890")
                    .address("123 Main St")
                    .build();

            when(patientService.getPatientProfile()).thenReturn(profileDto);

            mockMvc.perform(get("/api/patients/me")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.patientId").value(1))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"))
                    .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                    .andExpect(jsonPath("$.gender").value("MALE"));
        }

        @Test
        @WithMockUser(username = "john.doe@example.com")
        void fallsBackToUserWhenPatientMissing() throws Exception {
            PatientProfileDto fallback = PatientProfileDto.builder()
                    .patientId(42L)
                    .firstName("Fallback")
                    .lastName("User")
                    .email("john.doe@example.com")
                    .build();

            when(patientService.getPatientProfile()).thenReturn(fallback);

            mockMvc.perform(get("/api/patients/me")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("Fallback"))
                    .andExpect(jsonPath("$.email").value("john.doe@example.com"));
        }
    }

    @Nested
    @DisplayName("PUT /api/patients/me")
    class UpdateProfile {
        @Test
        @WithMockUser(username = "john.doe@example.com")
        void updatePatientProfile_Success() throws Exception {
            UpdatePatientProfileDto updateDto = new UpdatePatientProfileDto();
            updateDto.setFirstName("Jane");
            updateDto.setLastName("Smith");
            updateDto.setEmail("jane.smith@example.com");
            updateDto.setDateOfBirth(LocalDate.of(1985, 5, 15));
            updateDto.setGender(Patient.Gender.FEMALE);
            updateDto.setContactNumber("+9876543210");
            updateDto.setAddress("456 Oak Ave");

            PatientProfileDto updatedProfile = PatientProfileDto.builder()
                    .patientId(1L)
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("jane.smith@example.com")
                    .dateOfBirth(LocalDate.of(1985, 5, 15))
                    .gender(Patient.Gender.FEMALE)
                    .contactNumber("+9876543210")
                    .address("456 Oak Ave")
                    .build();

            when(patientService.updatePatientProfile(any(UpdatePatientProfileDto.class)))
                    .thenReturn(updatedProfile);

            mockMvc.perform(put("/api/patients/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("Jane"))
                    .andExpect(jsonPath("$.email").value("jane.smith@example.com"));
        }

        @Test
        @WithMockUser(username = "john.doe@example.com")
        void updatePatientProfile_InvalidData_ReturnsBadRequest() throws Exception {
            UpdatePatientProfileDto updateDto = new UpdatePatientProfileDto();
            updateDto.setFirstName(""); // invalid
            updateDto.setLastName("");
            updateDto.setEmail("invalid-email");

            mockMvc.perform(put("/api/patients/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isBadRequest());
        }
    }
}
