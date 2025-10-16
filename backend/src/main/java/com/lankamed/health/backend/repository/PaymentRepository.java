package com.lankamed.health.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lankamed.health.backend.model.Payment;
import com.lankamed.health.backend.model.PaymentStatus;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByPatient_PatientIdAndStatus(Long patientId, PaymentStatus status);
}
