package com.lankamed.health.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lankamed.health.backend.dto.CreateWaitlistDto;
import com.lankamed.health.backend.dto.WaitlistEntryDto;
import com.lankamed.health.backend.model.WaitlistEntry;
import com.lankamed.health.backend.service.WaitlistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WaitlistController.class,
        excludeFilters = @ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.REGEX,
                                              pattern = "com\\.lankamed\\.health\\.backend\\.security\\..*"))
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
    "feature.waitlist.enabled=true"
})
class WaitlistControllerEnabledTest {

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
        WaitlistEntryDto entry = WaitlistEntryDto.builder().id(100L).build();
        when(waitlistService.addToWaitlist(dto)).thenReturn(entry);

        mockMvc.perform(post("/api/patients/me/waitlist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("100"));
    }

    @Test
    void getMyWaitlist_featureEnabled_success() throws Exception {
        WaitlistEntryDto entry = WaitlistEntryDto.builder()
                .id(1L)
                .status(WaitlistEntry.Status.QUEUED)
                .build();
        when(waitlistService.getMyWaitlist()).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/patients/me/waitlist"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(entry))));
    }
}
