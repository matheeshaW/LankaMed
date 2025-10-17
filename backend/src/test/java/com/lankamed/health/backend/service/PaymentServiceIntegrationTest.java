package com.lankamed.health.backend.service;

import com.lankamed.health.backend.dto.PaymentDTO;
import com.lankamed.health.backend.model.Appointment;
import com.lankamed.health.backend.model.Payment;
import com.lankamed.health.backend.model.PaymentStatus;
import com.lankamed.health.backend.model.PaymentType;
import com.lankamed.health.backend.model.ServiceCategory;
import com.lankamed.health.backend.model.User;
import com.lankamed.health.backend.model.patient.Patient;
import com.lankamed.health.backend.repository.AppointmentRepository;
import com.lankamed.health.backend.repository.PaymentRepository;
import com.lankamed.health.backend.repository.patient.PatientRepository;
import com.lankamed.health.backend.strategy.MockPaymentStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for PaymentService that test the actual database interactions
 * and the complete payment flow with real repositories.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaymentServiceIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private MockPaymentStrategy mockPaymentStrategy;

    private Patient testPatient;
    private Appointment testAppointment;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        paymentRepository.deleteAll();
        appointmentRepository.deleteAll();
        patientRepository.deleteAll();

        // Create test patient
        testPatient = new Patient();
        User user = new User();
        user.setFirstName("Integration");
        user.setLastName("Test");
        user.setEmail("integration.test@example.com");
        testPatient.setUser(user);
        testPatient = patientRepository.save(testPatient);

        // Create test appointment
        testAppointment = new Appointment();
        testAppointment.setPatient(testPatient);
        testAppointment.setPaymentAmount(1500.00);
        testAppointment.setAppointmentDateTime(LocalDateTime.now().plusDays(1));

        ServiceCategory serviceCategory = new ServiceCategory();
        serviceCategory.setName("Integration Test Service");
        testAppointment.setServiceCategory(serviceCategory);

        testAppointment = appointmentRepository.save(testAppointment);

        // Reset mock strategy
        mockPaymentStrategy.reset();
    }

    @Test
    @DisplayName("Integration test - complete payment flow with database persistence")
    void integrationTest_completePaymentFlow() {
        // Given
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPatientId(testPatient.getPatientId());
        paymentDTO.setAppointmentId(testAppointment.getAppointmentId());
        paymentDTO.setAmount(1500.00);
        paymentDTO.setPaymentMethod("Card");
        paymentDTO.setCardNumber("1234567890123456");

        // When
        PaymentDTO result = paymentService.makePayment(paymentDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPatientId()).isEqualTo(testPatient.getPatientId());
        assertThat(result.getAppointmentId()).isEqualTo(testAppointment.getAppointmentId());
        assertThat(result.getAmount()).isEqualTo(1500.00);
        assertThat(result.getPaymentMethod()).isEqualTo("Card");
        assertThat(result.getStatus()).isEqualTo("Paid");
        assertThat(result.getTransactionId()).isNotNull();

        // Verify payment was persisted in database
        List<Payment> savedPayments = paymentRepository.findAll();
        assertThat(savedPayments).hasSize(1);
        
        Payment savedPayment = savedPayments.get(0);
        assertThat(savedPayment.getPatient().getPatientId()).isEqualTo(testPatient.getPatientId());
        assertThat(savedPayment.getAppointment().getAppointmentId()).isEqualTo(testAppointment.getAppointmentId());
        assertThat(savedPayment.getAmount()).isEqualTo(1500.00);
        assertThat(savedPayment.getPaymentType()).isEqualTo(PaymentType.Card);
        assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.Paid);
        assertThat(savedPayment.getTransactionId()).isEqualTo(result.getTransactionId());
        assertThat(savedPayment.getPaymentTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Integration test - get pending payments from database")
    void integrationTest_getPendingPayments() {
        // Given - create a pending payment directly in database
        Payment pendingPayment = new Payment();
        pendingPayment.setPatient(testPatient);
        pendingPayment.setAppointment(testAppointment);
        pendingPayment.setAmount(2000.00);
        pendingPayment.setPaymentType(PaymentType.Insurance);
        pendingPayment.setStatus(PaymentStatus.Pending);
        pendingPayment.setTransactionId("TXN-PENDING-001");
        pendingPayment.setPaymentTimestamp(LocalDateTime.now());
        paymentRepository.save(pendingPayment);

        // When
        List<PaymentDTO> result = paymentService.getPendingPayments(testPatient.getPatientId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        
        PaymentDTO paymentDTO = result.get(0);
        assertThat(paymentDTO.getPatientId()).isEqualTo(testPatient.getPatientId());
        assertThat(paymentDTO.getAppointmentId()).isEqualTo(testAppointment.getAppointmentId());
        assertThat(paymentDTO.getAmount()).isEqualTo(2000.00);
        assertThat(paymentDTO.getPaymentMethod()).isEqualTo("Insurance");
        assertThat(paymentDTO.getStatus()).isEqualTo("Pending");
        assertThat(paymentDTO.getTransactionId()).isEqualTo("TXN-PENDING-001");
    }

    @Test
    @DisplayName("Integration test - multiple pending payments for same patient")
    void integrationTest_multiplePendingPayments() {
        // Given - create multiple pending payments
        Payment payment1 = new Payment();
        payment1.setPatient(testPatient);
        payment1.setAppointment(testAppointment);
        payment1.setAmount(1000.00);
        payment1.setPaymentType(PaymentType.Card);
        payment1.setStatus(PaymentStatus.Pending);
        payment1.setTransactionId("TXN-PENDING-001");
        payment1.setPaymentTimestamp(LocalDateTime.now());
        paymentRepository.save(payment1);

        Payment payment2 = new Payment();
        payment2.setPatient(testPatient);
        payment2.setAppointment(testAppointment);
        payment2.setAmount(1500.00);
        payment2.setPaymentType(PaymentType.Cash);
        payment2.setStatus(PaymentStatus.Pending);
        payment2.setTransactionId("TXN-PENDING-002");
        payment2.setPaymentTimestamp(LocalDateTime.now());
        paymentRepository.save(payment2);

        // When
        List<PaymentDTO> result = paymentService.getPendingPayments(testPatient.getPatientId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        
        // Verify both payments are returned
        assertThat(result).extracting(PaymentDTO::getTransactionId)
                .containsExactlyInAnyOrder("TXN-PENDING-001", "TXN-PENDING-002");
        
        assertThat(result).extracting(PaymentDTO::getAmount)
                .containsExactlyInAnyOrder(1000.00, 1500.00);
    }

    @Test
    @DisplayName("Integration test - payment without appointment")
    void integrationTest_paymentWithoutAppointment() {
        // Given
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPatientId(testPatient.getPatientId());
        paymentDTO.setAppointmentId(null);
        paymentDTO.setAmount(500.00);
        paymentDTO.setPaymentMethod("Cash");

        // When
        PaymentDTO result = paymentService.makePayment(paymentDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPatientId()).isEqualTo(testPatient.getPatientId());
        assertThat(result.getAppointmentId()).isNull();
        assertThat(result.getAmount()).isEqualTo(500.00);
        assertThat(result.getPaymentMethod()).isEqualTo("Cash");
        assertThat(result.getStatus()).isEqualTo("Paid");

        // Verify payment was persisted without appointment
        List<Payment> savedPayments = paymentRepository.findAll();
        assertThat(savedPayments).hasSize(1);
        
        Payment savedPayment = savedPayments.get(0);
        assertThat(savedPayment.getAppointment()).isNull();
        assertThat(savedPayment.getPatient().getPatientId()).isEqualTo(testPatient.getPatientId());
    }

    @Test
    @DisplayName("Integration test - failed payment handling")
    void integrationTest_failedPayment() {
        // Given
        mockPaymentStrategy.setReturnStatus(PaymentStatus.Failed);
        
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPatientId(testPatient.getPatientId());
        paymentDTO.setAppointmentId(testAppointment.getAppointmentId());
        paymentDTO.setAmount(1500.00);
        paymentDTO.setPaymentMethod("Card");

        // When
        PaymentDTO result = paymentService.makePayment(paymentDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("Failed");

        // Verify failed payment was still persisted
        List<Payment> savedPayments = paymentRepository.findAll();
        assertThat(savedPayments).hasSize(1);
        
        Payment savedPayment = savedPayments.get(0);
        assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.Failed);
    }

    @Test
    @DisplayName("Integration test - patient not found scenario")
    void integrationTest_patientNotFound() {
        // Given
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPatientId(999L); // Non-existent patient ID
        paymentDTO.setAppointmentId(testAppointment.getAppointmentId());
        paymentDTO.setAmount(1500.00);
        paymentDTO.setPaymentMethod("Card");

        // When & Then
        assertThatThrownBy(() -> paymentService.makePayment(paymentDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Patient not found with ID: 999");

        // Verify no payment was persisted
        List<Payment> savedPayments = paymentRepository.findAll();
        assertThat(savedPayments).isEmpty();
    }

    @Test
    @DisplayName("Integration test - appointment not found scenario")
    void integrationTest_appointmentNotFound() {
        // Given
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPatientId(testPatient.getPatientId());
        paymentDTO.setAppointmentId(999L); // Non-existent appointment ID
        paymentDTO.setAmount(1500.00);
        paymentDTO.setPaymentMethod("Card");

        // When & Then
        assertThatThrownBy(() -> paymentService.makePayment(paymentDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Appointment not found with ID: 999");

        // Verify no payment was persisted
        List<Payment> savedPayments = paymentRepository.findAll();
        assertThat(savedPayments).isEmpty();
    }

    @Test
    @DisplayName("Integration test - get pending payments for non-existent patient")
    void integrationTest_getPendingPaymentsNonExistentPatient() {
        // When
        List<PaymentDTO> result = paymentService.getPendingPayments(999L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Integration test - transaction ID uniqueness")
    void integrationTest_transactionIdUniqueness() {
        // Given
        PaymentDTO paymentDTO1 = new PaymentDTO();
        paymentDTO1.setPatientId(testPatient.getPatientId());
        paymentDTO1.setAppointmentId(testAppointment.getAppointmentId());
        paymentDTO1.setAmount(1000.00);
        paymentDTO1.setPaymentMethod("Card");

        PaymentDTO paymentDTO2 = new PaymentDTO();
        paymentDTO2.setPatientId(testPatient.getPatientId());
        paymentDTO2.setAppointmentId(testAppointment.getAppointmentId());
        paymentDTO2.setAmount(2000.00);
        paymentDTO2.setPaymentMethod("Card");

        // When
        PaymentDTO result1 = paymentService.makePayment(paymentDTO1);
        PaymentDTO result2 = paymentService.makePayment(paymentDTO2);

        // Then
        assertThat(result1.getTransactionId()).isNotEqualTo(result2.getTransactionId());
        assertThat(result1.getTransactionId()).isNotNull();
        assertThat(result2.getTransactionId()).isNotNull();

        // Verify both payments were persisted with unique transaction IDs
        List<Payment> savedPayments = paymentRepository.findAll();
        assertThat(savedPayments).hasSize(2);
        
        assertThat(savedPayments).extracting(Payment::getTransactionId)
                .containsExactlyInAnyOrder(result1.getTransactionId(), result2.getTransactionId());
    }

    @Test
    @DisplayName("Integration test - payment timestamp accuracy")
    void integrationTest_paymentTimestampAccuracy() {
        // Given
        LocalDateTime beforePayment = LocalDateTime.now();
        
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPatientId(testPatient.getPatientId());
        paymentDTO.setAppointmentId(testAppointment.getAppointmentId());
        paymentDTO.setAmount(1500.00);
        paymentDTO.setPaymentMethod("Card");

        // When
        paymentService.makePayment(paymentDTO);
        
        LocalDateTime afterPayment = LocalDateTime.now();

        // Then
        List<Payment> savedPayments = paymentRepository.findAll();
        assertThat(savedPayments).hasSize(1);
        
        Payment savedPayment = savedPayments.get(0);
        assertThat(savedPayment.getPaymentTimestamp()).isNotNull();
        assertThat(savedPayment.getPaymentTimestamp()).isAfterOrEqualTo(beforePayment);
        assertThat(savedPayment.getPaymentTimestamp()).isBeforeOrEqualTo(afterPayment);
    }
}
