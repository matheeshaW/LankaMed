package com.lankamed.health.backend.repository.patient;

import com.lankamed.health.backend.model.patient.Patient;
import com.lankamed.health.backend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByPatientId(Long patientId);
    Optional<Patient> findByUserEmail(String email);
    List<Patient> findByUserRole(Role role);
}


