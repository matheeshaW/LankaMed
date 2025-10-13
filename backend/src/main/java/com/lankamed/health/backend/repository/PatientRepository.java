package com.lankamed.health.backend.repository;

import com.lankamed.health.backend.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByPatientId(Long patientId);
    Optional<Patient> findByUserEmail(String email);
}
