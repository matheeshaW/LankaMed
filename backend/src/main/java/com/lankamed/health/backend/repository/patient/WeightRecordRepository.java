package com.lankamed.health.backend.repository.patient;

import com.lankamed.health.backend.model.patient.WeightRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WeightRecordRepository extends JpaRepository<WeightRecord, Long> {
    List<WeightRecord> findByPatientUserEmail(String email);
}
