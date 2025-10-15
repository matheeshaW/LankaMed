package com.lankamed.health.backend.repository.patient;

import com.lankamed.health.backend.model.patient.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByPatientPatientId(Long patientId);
    List<Prescription> findByPatientUserEmail(String email);
    List<Prescription> findByDoctorStaffId(Long doctorId);
}
