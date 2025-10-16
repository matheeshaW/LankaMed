package com.lankamed.health.backend.repository.patient;

import com.lankamed.health.backend.model.patient.MedicalCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalConditionRepository extends JpaRepository<MedicalCondition, Long> {
    List<MedicalCondition> findByPatientPatientId(Long patientId);
    List<MedicalCondition> findByPatientUserEmail(String email);
}
