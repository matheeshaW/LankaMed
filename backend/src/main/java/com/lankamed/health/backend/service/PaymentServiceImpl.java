package com.lankamed.health.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lankamed.health.backend.dto.PaymentDTO;
import com.lankamed.health.backend.model.Appointment;
import com.lankamed.health.backend.model.Payment;
import com.lankamed.health.backend.model.PaymentStatus;
import com.lankamed.health.backend.model.PaymentType;
import com.lankamed.health.backend.model.patient.Patient;
import com.lankamed.health.backend.repository.AppointmentRepository;
import com.lankamed.health.backend.repository.PaymentRepository;
import com.lankamed.health.backend.repository.patient.PatientRepository;
import com.lankamed.health.backend.strategy.PaymentStrategy;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final Map<String, PaymentStrategy> paymentStrategies;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository, 
                            PatientRepository patientRepository,
                            AppointmentRepository appointmentRepository,
                            List<PaymentStrategy> strategies) {
        this.paymentRepository = paymentRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.paymentStrategies = strategies.stream()
                .collect(Collectors.toMap(
                        s -> s.getClass().getSimpleName().replace("Payment", ""),
                        Function.identity()));
    }

    @Override
    public PaymentDTO makePayment(PaymentDTO dto) {
        // Validate payment method
        PaymentStrategy strategy = paymentStrategies.get(dto.getPaymentMethod());
        if (strategy == null) {
            throw new IllegalArgumentException("Invalid payment method: " + dto.getPaymentMethod());
        }

        // Fetch patient from database
        Patient patient = patientRepository.findByPatientId(dto.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with ID: " + dto.getPatientId()));

        // Fetch appointment from database if provided
        Appointment appointment = null;
        if (dto.getAppointmentId() != null) {
            appointment = appointmentRepository.findById(dto.getAppointmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Appointment not found with ID: " + dto.getAppointmentId()));
        }

        // Process payment using strategy pattern
        PaymentStatus status = strategy.processPayment(dto);

        // Create new payment entity
        Payment payment = new Payment();
        payment.setPatient(patient);
        payment.setAppointment(appointment);
        payment.setAmount(dto.getAmount());
        payment.setPaymentType(PaymentType.valueOf(dto.getPaymentMethod()));
        payment.setStatus(status);
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setPaymentTimestamp(LocalDateTime.now());

        // Save payment to database
        payment = paymentRepository.save(payment);

        // Update DTO with generated values
        dto.setTransactionId(payment.getTransactionId());
        dto.setStatus(status.name());

        return dto;
    }

    @Override
    public List<PaymentDTO> getPendingPayments(Long patientId) {
        return paymentRepository.findByPatient_PatientIdAndStatus(patientId, PaymentStatus.Pending)
                .stream()
                .map(p -> new PaymentDTO(
                        p.getPatient().getPatientId(),
                        p.getAppointment() != null ? p.getAppointment().getAppointmentId() : null,
                        p.getAmount(),
                        p.getPaymentType().name(),
                        p.getTransactionId(),
                        p.getStatus().name()
                ))
                .collect(Collectors.toList());
    }
}
