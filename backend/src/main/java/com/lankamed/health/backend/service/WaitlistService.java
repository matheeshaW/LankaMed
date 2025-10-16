package com.lankamed.health.backend.service;

import com.lankamed.health.backend.dto.CreateWaitlistDto;
import com.lankamed.health.backend.dto.WaitlistEntryDto;
import java.util.List;
import java.util.Optional;

public interface WaitlistService {
    WaitlistEntryDto addToWaitlist(CreateWaitlistDto dto);
    List<WaitlistEntryDto> getMyWaitlist();
    WaitlistEntryDto promoteToAppointment(Long waitlistId);

    WaitlistEntryDto updateWaitlistStatus(Long waitlistId, String newStatus);
    
    // Additional methods for admin
    List<WaitlistEntryDto> getAllQueuedWaitlistEntries();
    List<WaitlistEntryDto> getQueuedWaitlistEntriesByDoctor(Long doctorId);
    List<WaitlistEntryDto> getAllActiveWaitlistEntries(); // Excludes PROMOTED entries
    Optional<WaitlistEntryDto> promoteNextToAppointment(Long doctorId);
    
    // Alias methods for backward compatibility
    default List<WaitlistEntryDto> listAllQueued() {
        return getAllQueuedWaitlistEntries();
    }
    
    default List<WaitlistEntryDto> listQueuedByDoctor(Long doctorId) {
        return getQueuedWaitlistEntriesByDoctor(doctorId);
    }
}
