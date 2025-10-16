package com.lankamed.health.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lankamed.health.backend.dto.CreateWaitlistDto;
import com.lankamed.health.backend.dto.WaitlistEntryDto;
import com.lankamed.health.backend.service.WaitlistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WaitlistController.class)
@AutoConfigureMockMvc(addFilters = false)
class WaitlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WaitlistService waitlistService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void addToWaitlist_featureEnabled_success() throws Exception {
        CreateWaitlistDto dto = new CreateWaitlistDto();
        dto.setDoctorId(1L);
        dto.setHospitalId(2L);
        dto.setServiceCategoryId(3L);
        dto.setDesiredDateTime(LocalDateTime.now().plusDays(1));
        WaitlistEntryDto result = WaitlistEntryDto.builder().id(100L).build();
        when(waitlistService.addToWaitlist(dto)).thenReturn(result);

        mockMvc.perform(post("/api/patients/me/waitlist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("100"));
    }

    @Test
    void addToWaitlist_featureDisabled_fails() throws Exception {
        CreateWaitlistDto dto = new CreateWaitlistDto();
        dto.setDoctorId(1L);
        dto.setHospitalId(2L);
        dto.setServiceCategoryId(3L);
        dto.setDesiredDateTime(LocalDateTime.now().plusDays(1));
        when(waitlistService.addToWaitlist(dto)).thenThrow(new UnsupportedOperationException("Waitlist feature is disabled"));

        mockMvc.perform(post("/api/patients/me/waitlist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Waitlist feature disabled"));
    }

    @Test
    void getMyWaitlist_featureEnabled_success() throws Exception {
        WaitlistEntryDto entry = WaitlistEntryDto.builder()
                .id(1L)
                .desiredDateTime(LocalDateTime.now().plusDays(2))
                .build();
        when(waitlistService.getMyWaitlist()).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/patients/me/waitlist"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(entry))));
    }

    @Test
    void getMyWaitlist_featureDisabled_empty() throws Exception {
        when(waitlistService.getMyWaitlist()).thenReturn(List.of());

        mockMvc.perform(get("/api/patients/me/waitlist"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of())));
    }
}
