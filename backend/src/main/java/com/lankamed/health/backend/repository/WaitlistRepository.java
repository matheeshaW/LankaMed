package com.lankamed.health.backend.repository;

import com.lankamed.health.backend.model.WaitlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface WaitlistRepository extends JpaRepository<WaitlistEntry, Long> {
    List<WaitlistEntry> findByDoctorStaffIdAndDesiredDateTimeBetweenAndStatusOrderByCreatedAtAsc(
            Long doctorId, LocalDateTime from, LocalDateTime to, WaitlistEntry.Status status);

    List<WaitlistEntry> findByPatientUserEmailOrderByCreatedAtDesc(String email);
    
    List<WaitlistEntry> findByPatientUserEmailAndStatusNotOrderByCreatedAtDesc(String email, WaitlistEntry.Status status);

    List<WaitlistEntry> findByStatusOrderByCreatedAtAsc(WaitlistEntry.Status status);

    List<WaitlistEntry> findByDoctorStaffIdAndStatusOrderByCreatedAtAsc(Long doctorId, WaitlistEntry.Status status);
    
    List<WaitlistEntry> findByStatusNotOrderByCreatedAtAsc(WaitlistEntry.Status status);
}
