package com.lankamed.health.backend.controller.patient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lankamed.health.backend.dto.patient.EmergencyContactDto;
import com.lankamed.health.backend.dto.patient.CreateEmergencyContactDto;
import com.lankamed.health.backend.service.patient.EmergencyContactService;
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

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmergencyContactController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmergencyContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmergencyContactService emergencyContactService;

    // Mock security-related beans that are picked up by the application context
    @MockBean
    private com.lankamed.health.backend.security.JwtUtil jwtUtil;

    @MockBean
    private com.lankamed.health.backend.service.CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("GET /api/patients/me/emergency-contacts")
    class GetEmergencyContacts {
        @Test
        @WithMockUser(username = "patient@example.com")
        void getEmergencyContacts_Success() throws Exception {
            List<EmergencyContactDto> contacts = Arrays.asList(
                    EmergencyContactDto.builder()
                            .emergencyContactId(1L)
                            .fullName("John Doe")
                            .relationship("Father")
                            .phone("+1234567890")
                            .email("john@example.com")
                            .build(),
                    EmergencyContactDto.builder()
                            .emergencyContactId(2L)
                            .fullName("Jane Smith")
                            .relationship("Mother")
                            .phone("+0987654321")
                            .email("jane@example.com")
                            .build()
            );

            when(emergencyContactService.getEmergencyContacts()).thenReturn(contacts);

            mockMvc.perform(get("/api/patients/me/emergency-contacts")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].emergencyContactId").value(1))
                    .andExpect(jsonPath("$[0].fullName").value("John Doe"))
                    .andExpect(jsonPath("$[0].relationship").value("Father"))
                    .andExpect(jsonPath("$[1].emergencyContactId").value(2))
                    .andExpect(jsonPath("$[1].fullName").value("Jane Smith"))
                    .andExpect(jsonPath("$[1].relationship").value("Mother"));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void getEmergencyContacts_EmptyList() throws Exception {
            when(emergencyContactService.getEmergencyContacts()).thenReturn(Arrays.asList());

            mockMvc.perform(get("/api/patients/me/emergency-contacts")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("POST /api/patients/me/emergency-contacts")
    class CreateEmergencyContact {
        @Test
        @WithMockUser(username = "patient@example.com")
        void createEmergencyContact_Success() throws Exception {
            CreateEmergencyContactDto createDto = new CreateEmergencyContactDto();
            createDto.setFullName("John Doe");
            createDto.setRelationship("Father");
            createDto.setPhone("+1234567890");
            createDto.setEmail("john@example.com");

            EmergencyContactDto createdContact = EmergencyContactDto.builder()
                    .emergencyContactId(1L)
                    .fullName("John Doe")
                    .relationship("Father")
                    .phone("+1234567890")
                    .email("john@example.com")
                    .build();

            when(emergencyContactService.createEmergencyContact(any(CreateEmergencyContactDto.class)))
                    .thenReturn(createdContact);

            mockMvc.perform(post("/api/patients/me/emergency-contacts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.emergencyContactId").value(1))
                    .andExpect(jsonPath("$.fullName").value("John Doe"))
                    .andExpect(jsonPath("$.relationship").value("Father"))
                    .andExpect(jsonPath("$.phone").value("+1234567890"))
                    .andExpect(jsonPath("$.email").value("john@example.com"));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void createEmergencyContact_InvalidData() throws Exception {
            CreateEmergencyContactDto createDto = new CreateEmergencyContactDto();
            createDto.setFullName(""); // Invalid empty name
            createDto.setRelationship("");
            createDto.setPhone("invalid-phone");
            createDto.setEmail("invalid-email");

            mockMvc.perform(post("/api/patients/me/emergency-contacts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/patients/me/emergency-contacts/{emergencyContactId}")
    class UpdateEmergencyContact {
        @Test
        @WithMockUser(username = "patient@example.com")
        void updateEmergencyContact_Success() throws Exception {
            Long contactId = 1L;
            CreateEmergencyContactDto updateDto = new CreateEmergencyContactDto();
            updateDto.setFullName("John Updated");
            updateDto.setRelationship("Father");
            updateDto.setPhone("+1234567890");
            updateDto.setEmail("john.updated@example.com");

            EmergencyContactDto updatedContact = EmergencyContactDto.builder()
                    .emergencyContactId(contactId)
                    .fullName("John Updated")
                    .relationship("Father")
                    .phone("+1234567890")
                    .email("john.updated@example.com")
                    .build();

            when(emergencyContactService.updateEmergencyContact(eq(contactId), any(CreateEmergencyContactDto.class)))
                    .thenReturn(updatedContact);

            mockMvc.perform(put("/api/patients/me/emergency-contacts/{emergencyContactId}", contactId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.emergencyContactId").value(contactId))
                    .andExpect(jsonPath("$.fullName").value("John Updated"))
                    .andExpect(jsonPath("$.relationship").value("Father"))
                    .andExpect(jsonPath("$.phone").value("+1234567890"))
                    .andExpect(jsonPath("$.email").value("john.updated@example.com"));
        }

        @Test
        @WithMockUser(username = "patient@example.com")
        void updateEmergencyContact_InvalidData() throws Exception {
            Long contactId = 1L;
            CreateEmergencyContactDto updateDto = new CreateEmergencyContactDto();
            updateDto.setFullName(""); // Invalid empty name
            updateDto.setRelationship("");
            updateDto.setPhone("invalid-phone");
            updateDto.setEmail("invalid-email");

            mockMvc.perform(put("/api/patients/me/emergency-contacts/{emergencyContactId}", contactId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/patients/me/emergency-contacts/{emergencyContactId}")
    class DeleteEmergencyContact {
        @Test
        @WithMockUser(username = "patient@example.com")
        void deleteEmergencyContact_Success() throws Exception {
            Long contactId = 1L;

            mockMvc.perform(delete("/api/patients/me/emergency-contacts/{emergencyContactId}", contactId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());
        }
    }
}
