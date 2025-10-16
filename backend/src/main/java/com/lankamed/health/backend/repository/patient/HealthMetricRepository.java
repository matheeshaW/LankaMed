package com.lankamed.health.backend.repository.patient;

import com.lankamed.health.backend.model.patient.HealthMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthMetricRepository extends JpaRepository<HealthMetric, Long> {
    List<HealthMetric> findByPatientUserEmail(String email);
}
