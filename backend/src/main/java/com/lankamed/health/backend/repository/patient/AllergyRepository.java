package com.lankamed.health.backend.repository.patient;
import com.lankamed.health.backend.model.patient.Allergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllergyRepository extends JpaRepository<Allergy, Long> {
    List<Allergy> findByPatientPatientId(Long patientId);
    List<Allergy> findByPatientUserEmail(String email);
}
