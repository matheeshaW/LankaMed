package com.lankamed.health.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SlotAvailabilityDto {
    private Long doctorId;
    private String doctorName;
    private String date; // ISO yyyy-MM-dd
    private int capacity;
    private int booked;
    private int available;
}
