package com.lankamed.health.backend.repository.patient;

import com.lankamed.health.backend.model.patient.BloodPressureRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BloodPressureRecordRepository extends JpaRepository<BloodPressureRecord, Long> {
    List<BloodPressureRecord> findByPatientUserEmail(String email);
}
