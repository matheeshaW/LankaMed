package com.lankamed.health.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lankamed.health.backend.dto.PatientProfileDto;
import com.lankamed.health.backend.dto.UpdatePatientProfileDto;
import com.lankamed.health.backend.model.Patient;
import com.lankamed.health.backend.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientService patientService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "john.doe@example.com")
    void getPatientProfile_Success() throws Exception {
        // Given
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

        // When & Then
        mockMvc.perform(get("/api/patients/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.gender").value("MALE"))
                .andExpect(jsonPath("$.contactNumber").value("+1234567890"))
                .andExpect(jsonPath("$.address").value("123 Main St"));
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    void updatePatientProfile_Success() throws Exception {
        // Given
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

        // When & Then
        mockMvc.perform(put("/api/patients/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.email").value("jane.smith@example.com"))
                .andExpect(jsonPath("$.gender").value("FEMALE"))
                .andExpect(jsonPath("$.contactNumber").value("+9876543210"))
                .andExpect(jsonPath("$.address").value("456 Oak Ave"));
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    void updatePatientProfile_InvalidData() throws Exception {
        // Given
        UpdatePatientProfileDto updateDto = new UpdatePatientProfileDto();
        updateDto.setFirstName(""); // Invalid: empty first name
        updateDto.setLastName(""); // Invalid: empty last name
        updateDto.setEmail("invalid-email"); // Invalid: malformed email

        // When & Then
        mockMvc.perform(put("/api/patients/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest());
    }
}
