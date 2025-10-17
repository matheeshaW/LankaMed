package com.lankamed.health.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lankamed.health.backend.dto.SlotAvailabilityDto;
import com.lankamed.health.backend.service.DoctorSlotService;
import com.lankamed.health.backend.service.WaitlistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminWaitlistController.class, 
           excludeFilters = @ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.REGEX, 
                                                 pattern = "com\\.lankamed\\.health\\.backend\\.security\\..*"))
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
    "feature.waitlist.enabled=false"
})
class AdminWaitlistControllerDisabledTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WaitlistService waitlistService;

    @MockBean
    private DoctorSlotService doctorSlotService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void promoteToAppointment_featureDisabled_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/admin/waitlist/1/promote"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Waitlist feature disabled"));
    }

    @Test
    void queue_featureDisabled_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/admin/waitlist/queue/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    void all_featureDisabled_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/admin/waitlist/all"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    void updateWaitlistStatus_featureDisabled_returnsBadRequest() throws Exception {
        mockMvc.perform(put("/api/admin/waitlist/1/status")
                .contentType("application/json")
                .content("{\"status\": \"QUEUED\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Waitlist feature disabled"));
    }

    @Test
    void availability_worksEvenWhenFeatureDisabled() throws Exception {
        SlotAvailabilityDto dto = SlotAvailabilityDto.builder()
                .doctorId(5L)
                .date(LocalDate.now().toString())
                .capacity(10)
                .booked(7)
                .available(3)
                .build();
        when(doctorSlotService.getAvailability(5L, LocalDate.parse(dto.getDate()))).thenReturn(dto);

        mockMvc.perform(get("/api/admin/waitlist/availability/5").param("date", dto.getDate()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(dto)));
    }
}
