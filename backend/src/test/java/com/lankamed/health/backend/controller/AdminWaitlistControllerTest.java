package com.lankamed.health.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lankamed.health.backend.dto.SlotAvailabilityDto;
import com.lankamed.health.backend.dto.WaitlistEntryDto;
import com.lankamed.health.backend.model.WaitlistEntry;
import com.lankamed.health.backend.service.DoctorSlotService;
import com.lankamed.health.backend.service.WaitlistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminWaitlistController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminWaitlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WaitlistService waitlistService;

    @MockBean
    private DoctorSlotService doctorSlotService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void promoteToAppointment_featureEnabled_success() throws Exception {
        WaitlistEntryDto result = WaitlistEntryDto.builder()
                .id(1L)
                .status(WaitlistEntry.Status.PROMOTED)
                .build();
        when(waitlistService.promoteToAppointment(1L)).thenReturn(result);

        mockMvc.perform(post("/api/admin/waitlist/1/promote"))
                .andExpect(status().isOk())
                .andExpect(content().string("PROMOTED"));
    }

    @Test
    void promoteToAppointment_featureDisabled_fails() throws Exception {
        when(waitlistService.promoteToAppointment(1L))
                .thenThrow(new UnsupportedOperationException("Waitlist feature is disabled"));

        mockMvc.perform(post("/api/admin/waitlist/1/promote"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Waitlist feature disabled"));
    }

    @Test
    void availability_returnsDto() throws Exception {
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
