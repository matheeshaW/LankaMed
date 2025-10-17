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
import com.lankamed.health.backend.strategy.PaymentStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PaymentStrategy cardPaymentStrategy;

    @Mock
    private PaymentStrategy insurancePaymentStrategy;

    @Mock
    private PaymentStrategy cashPaymentStrategy;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private PaymentDTO validPaymentDTO;
    private Patient testPatient;
    private Appointment testAppointment;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        // Setup test patient
        testPatient = new Patient();
        testPatient.setPatientId(1L);
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        testPatient.setUser(user);

        // Setup test appointment
        testAppointment = new Appointment();
        testAppointment.setAppointmentId(100L);
        testAppointment.setPatient(testPatient);
        testAppointment.setPaymentAmount(1500.00);
        testAppointment.setAppointmentDateTime(LocalDateTime.now().plusDays(1));

        // Setup service category
        ServiceCategory serviceCategory = new ServiceCategory();
        serviceCategory.setName("General Consultation");
        testAppointment.setServiceCategory(serviceCategory);

        // Setup test payment
        testPayment = new Payment();
        testPayment.setPaymentId(1L);
        testPayment.setPatient(testPatient);
        testPayment.setAppointment(testAppointment);
        testPayment.setAmount(1500.00);
        testPayment.setPaymentType(PaymentType.Card);
        testPayment.setStatus(PaymentStatus.Pending);
        testPayment.setTransactionId("TXN-123456789");
        testPayment.setPaymentTimestamp(LocalDateTime.now());

        // Setup valid payment DTO
        validPaymentDTO = new PaymentDTO();
        validPaymentDTO.setPatientId(1L);
        validPaymentDTO.setAppointmentId(100L);
        validPaymentDTO.setAmount(1500.00);
        validPaymentDTO.setPaymentMethod("Card");
        validPaymentDTO.setCardNumber("1234567890123456");

        // Setup payment strategies map
        Map<String, PaymentStrategy> strategies = new HashMap<>();
        strategies.put("Card", cardPaymentStrategy);
        strategies.put("Insurance", insurancePaymentStrategy);
        strategies.put("Cash", cashPaymentStrategy);

        // Use reflection to set the strategies map
        try {
            java.lang.reflect.Field field = PaymentServiceImpl.class.getDeclaredField("paymentStrategies");
            field.setAccessible(true);
            field.set(paymentService, strategies);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set payment strategies", e);
        }
    }

    @Test
    @DisplayName("makePayment - processes card payment successfully")
    void makePayment_cardPayment_success() {
        // Given
        when(patientRepository.findByPatientId(1L)).thenReturn(Optional.of(testPatient));
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(testAppointment));
        when(cardPaymentStrategy.processPayment(any(PaymentDTO.class))).thenReturn(PaymentStatus.Paid);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        PaymentDTO result = paymentService.makePayment(validPaymentDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPatientId()).isEqualTo(1L);
        assertThat(result.getAppointmentId()).isEqualTo(100L);
        assertThat(result.getAmount()).isEqualTo(1500.00);
        assertThat(result.getPaymentMethod()).isEqualTo("Card");
        assertThat(result.getStatus()).isEqualTo("Paid");
        assertThat(result.getTransactionId()).isEqualTo("TXN-123456789");

        verify(patientRepository).findByPatientId(1L);
        verify(appointmentRepository).findById(100L);
        verify(cardPaymentStrategy).processPayment(validPaymentDTO);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("makePayment - processes insurance payment successfully")
    void makePayment_insurancePayment_success() {
        // Given
        PaymentDTO insurancePaymentDTO = new PaymentDTO();
        insurancePaymentDTO.setPatientId(1L);
        insurancePaymentDTO.setAppointmentId(100L);
        insurancePaymentDTO.setAmount(2000.00);
        insurancePaymentDTO.setPaymentMethod("Insurance");
        insurancePaymentDTO.setInsuranceNumber("INS-123456");

        when(patientRepository.findByPatientId(1L)).thenReturn(Optional.of(testPatient));
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(testAppointment));
        when(insurancePaymentStrategy.processPayment(any(PaymentDTO.class))).thenReturn(PaymentStatus.Paid);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        PaymentDTO result = paymentService.makePayment(insurancePaymentDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPaymentMethod()).isEqualTo("Insurance");
        assertThat(result.getAmount()).isEqualTo(2000.00);
        assertThat(result.getStatus()).isEqualTo("Paid");

        verify(insurancePaymentStrategy).processPayment(insurancePaymentDTO);
    }

    @Test
    @DisplayName("makePayment - processes cash payment successfully")
    void makePayment_cashPayment_success() {
        // Given
        PaymentDTO cashPaymentDTO = new PaymentDTO();
        cashPaymentDTO.setPatientId(1L);
        cashPaymentDTO.setAppointmentId(100L);
        cashPaymentDTO.setAmount(1000.00);
        cashPaymentDTO.setPaymentMethod("Cash");

        when(patientRepository.findByPatientId(1L)).thenReturn(Optional.of(testPatient));
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(testAppointment));
        when(cashPaymentStrategy.processPayment(any(PaymentDTO.class))).thenReturn(PaymentStatus.Paid);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        PaymentDTO result = paymentService.makePayment(cashPaymentDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPaymentMethod()).isEqualTo("Cash");
        assertThat(result.getAmount()).isEqualTo(1000.00);
        assertThat(result.getStatus()).isEqualTo("Paid");

        verify(cashPaymentStrategy).processPayment(cashPaymentDTO);
    }

    @Test
    @DisplayName("makePayment - handles payment without appointment")
    void makePayment_withoutAppointment_success() {
        // Given
        PaymentDTO paymentWithoutAppointment = new PaymentDTO();
        paymentWithoutAppointment.setPatientId(1L);
        paymentWithoutAppointment.setAppointmentId(null);
        paymentWithoutAppointment.setAmount(500.00);
        paymentWithoutAppointment.setPaymentMethod("Cash");

        when(patientRepository.findByPatientId(1L)).thenReturn(Optional.of(testPatient));
        when(cashPaymentStrategy.processPayment(any(PaymentDTO.class))).thenReturn(PaymentStatus.Paid);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        PaymentDTO result = paymentService.makePayment(paymentWithoutAppointment);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAppointmentId()).isNull();
        assertThat(result.getAmount()).isEqualTo(500.00);

        verify(appointmentRepository, never()).findById(any());
    }

    @Test
    @DisplayName("makePayment - throws exception for invalid payment method")
    void makePayment_invalidPaymentMethod_throwsException() {
        // Given
        PaymentDTO invalidPaymentDTO = new PaymentDTO();
        invalidPaymentDTO.setPatientId(1L);
        invalidPaymentDTO.setAppointmentId(100L);
        invalidPaymentDTO.setAmount(1500.00);
        invalidPaymentDTO.setPaymentMethod("InvalidMethod");

        // When & Then
        assertThatThrownBy(() -> paymentService.makePayment(invalidPaymentDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid payment method: InvalidMethod");

        verify(patientRepository, never()).findByPatientId(any());
        verify(appointmentRepository, never()).findById(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("makePayment - throws exception when patient not found")
    void makePayment_patientNotFound_throwsException() {
        // Given
        when(patientRepository.findByPatientId(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> paymentService.makePayment(validPaymentDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Patient not found with ID: 1");

        verify(patientRepository).findByPatientId(1L);
        verify(appointmentRepository, never()).findById(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("makePayment - throws exception when appointment not found")
    void makePayment_appointmentNotFound_throwsException() {
        // Given
        when(patientRepository.findByPatientId(1L)).thenReturn(Optional.of(testPatient));
        when(appointmentRepository.findById(100L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> paymentService.makePayment(validPaymentDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Appointment not found with ID: 100");

        verify(patientRepository).findByPatientId(1L);
        verify(appointmentRepository).findById(100L);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("makePayment - handles failed payment status")
    void makePayment_failedPaymentStatus() {
        // Given
        when(patientRepository.findByPatientId(1L)).thenReturn(Optional.of(testPatient));
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(testAppointment));
        when(cardPaymentStrategy.processPayment(any(PaymentDTO.class))).thenReturn(PaymentStatus.Failed);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        PaymentDTO result = paymentService.makePayment(validPaymentDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("Failed");

        verify(cardPaymentStrategy).processPayment(validPaymentDTO);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("getPendingPayments - returns pending payments successfully")
    void getPendingPayments_success() {
        // Given
        List<Payment> pendingPayments = Arrays.asList(testPayment);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(paymentRepository.findByPatient_PatientIdAndStatus(1L, PaymentStatus.Pending))
                .thenReturn(pendingPayments);

        // When
        List<PaymentDTO> result = paymentService.getPendingPayments(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPatientId()).isEqualTo(1L);
        assertThat(result.get(0).getAppointmentId()).isEqualTo(100L);
        assertThat(result.get(0).getAmount()).isEqualTo(1500.00);
        assertThat(result.get(0).getStatus()).isEqualTo("Pending");

        verify(patientRepository).findById(1L);
        verify(paymentRepository).findByPatient_PatientIdAndStatus(1L, PaymentStatus.Pending);
    }

    @Test
    @DisplayName("getPendingPayments - returns empty list when no pending payments")
    void getPendingPayments_emptyList() {
        // Given
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(paymentRepository.findByPatient_PatientIdAndStatus(1L, PaymentStatus.Pending))
                .thenReturn(Collections.emptyList());

        // When
        List<PaymentDTO> result = paymentService.getPendingPayments(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(patientRepository).findById(1L);
        verify(paymentRepository).findByPatient_PatientIdAndStatus(1L, PaymentStatus.Pending);
    }

    @Test
    @DisplayName("getPendingPayments - returns empty list when patient not found")
    void getPendingPayments_patientNotFound() {
        // Given
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());
        when(patientRepository.findByPatientId(1L)).thenReturn(Optional.empty());

        // When
        List<PaymentDTO> result = paymentService.getPendingPayments(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(patientRepository).findById(1L);
        verify(patientRepository).findByPatientId(1L);
        verify(paymentRepository, never()).findByPatient_PatientIdAndStatus(any(), any());
    }

    @Test
    @DisplayName("getPendingPayments - handles primary method failure and uses alternative")
    void getPendingPayments_primaryMethodFailure_usesAlternative() {
        // Given
        List<Payment> pendingPayments = Arrays.asList(testPayment);
        when(patientRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));
        when(patientRepository.findByPatientId(1L)).thenReturn(Optional.of(testPatient));
        when(paymentRepository.findByPatient_PatientIdAndStatus(1L, PaymentStatus.Pending))
                .thenReturn(pendingPayments);

        // When
        List<PaymentDTO> result = paymentService.getPendingPayments(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(patientRepository).findById(1L);
        verify(patientRepository).findByPatientId(1L);
        verify(paymentRepository).findByPatient_PatientIdAndStatus(1L, PaymentStatus.Pending);
    }

    @Test
    @DisplayName("getPendingPayments - handles alternative method failure and uses general query")
    void getPendingPayments_alternativeMethodFailure_usesGeneralQuery() {
        // Given
        List<Payment> allPayments = Arrays.asList(testPayment);
        when(patientRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));
        when(patientRepository.findByPatientId(1L)).thenThrow(new RuntimeException("Database error"));
        when(paymentRepository.findByPatient_PatientIdAndStatus(1L, PaymentStatus.Pending))
                .thenThrow(new RuntimeException("Query error"));
        when(paymentRepository.findByPatientPatientIdAndStatus(1L, PaymentStatus.Pending))
                .thenThrow(new RuntimeException("Alternative query error"));
        when(paymentRepository.findAll()).thenReturn(allPayments);

        // When
        List<PaymentDTO> result = paymentService.getPendingPayments(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(paymentRepository).findAll();
    }

    @Test
    @DisplayName("getPendingPayments - handles all query methods failure and returns empty list")
    void getPendingPayments_allMethodsFailure_returnsEmptyList() {
        // Given
        when(patientRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));
        when(patientRepository.findByPatientId(1L)).thenThrow(new RuntimeException("Database error"));
        when(paymentRepository.findByPatient_PatientIdAndStatus(1L, PaymentStatus.Pending))
                .thenThrow(new RuntimeException("Query error"));
        when(paymentRepository.findByPatientPatientIdAndStatus(1L, PaymentStatus.Pending))
                .thenThrow(new RuntimeException("Alternative query error"));
        when(paymentRepository.findAll()).thenThrow(new RuntimeException("General query error"));

        // When
        List<PaymentDTO> result = paymentService.getPendingPayments(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getPendingPayments - includes appointment details in DTO")
    void getPendingPayments_includesAppointmentDetails() {
        // Given
        // Setup appointment with doctor details
        User doctorUser = new User();
        doctorUser.setFirstName("Jane");
        doctorUser.setLastName("Smith");

        com.lankamed.health.backend.model.StaffDetails doctor = new com.lankamed.health.backend.model.StaffDetails();
        doctor.setUser(doctorUser);
        testAppointment.setDoctor(doctor);

        // Setup hospital
        com.lankamed.health.backend.model.Hospital hospital = new com.lankamed.health.backend.model.Hospital();
        hospital.setName("City Hospital");
        testAppointment.setHospital(hospital);

        List<Payment> pendingPayments = Arrays.asList(testPayment);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(paymentRepository.findByPatient_PatientIdAndStatus(1L, PaymentStatus.Pending))
                .thenReturn(pendingPayments);

        // When
        List<PaymentDTO> result = paymentService.getPendingPayments(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        
        PaymentDTO paymentDTO = result.get(0);
        assertThat(paymentDTO.getDoctorName()).isEqualTo("Jane Smith");
        assertThat(paymentDTO.getServiceName()).isEqualTo("General Consultation");
        assertThat(paymentDTO.getHospitalName()).isEqualTo("City Hospital");
        assertThat(paymentDTO.getAppointmentDescription()).contains("General Consultation");
        assertThat(paymentDTO.getAppointmentDescription()).contains("Dr. Jane Smith");
    }

    @Test
    @DisplayName("getPendingPayments - handles payment without appointment")
    void getPendingPayments_paymentWithoutAppointment() {
        // Given
        Payment paymentWithoutAppointment = new Payment();
        paymentWithoutAppointment.setPaymentId(2L);
        paymentWithoutAppointment.setPatient(testPatient);
        paymentWithoutAppointment.setAppointment(null);
        paymentWithoutAppointment.setAmount(500.00);
        paymentWithoutAppointment.setPaymentType(PaymentType.Cash);
        paymentWithoutAppointment.setStatus(PaymentStatus.Pending);
        paymentWithoutAppointment.setTransactionId("TXN-CASH-001");
        paymentWithoutAppointment.setPaymentTimestamp(LocalDateTime.now());

        List<Payment> pendingPayments = Arrays.asList(paymentWithoutAppointment);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(paymentRepository.findByPatient_PatientIdAndStatus(1L, PaymentStatus.Pending))
                .thenReturn(pendingPayments);

        // When
        List<PaymentDTO> result = paymentService.getPendingPayments(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        
        PaymentDTO paymentDTO = result.get(0);
        assertThat(paymentDTO.getAppointmentId()).isNull();
        assertThat(paymentDTO.getDoctorName()).isNull();
        assertThat(paymentDTO.getServiceName()).isNull();
        assertThat(paymentDTO.getHospitalName()).isNull();
        assertThat(paymentDTO.getAppointmentDescription()).isNull();
    }

    @Test
    @DisplayName("getPendingPayments - handles individual payment processing error gracefully")
    void getPendingPayments_individualPaymentError_continuesProcessing() {
        // Given
        Payment validPayment = testPayment;
        Payment invalidPayment = new Payment();
        invalidPayment.setPaymentId(2L);
        invalidPayment.setPatient(null); // This will cause an error
        invalidPayment.setAmount(500.00);
        invalidPayment.setPaymentType(PaymentType.Cash);
        invalidPayment.setStatus(PaymentStatus.Pending);
        invalidPayment.setTransactionId("TXN-INVALID-001");
        invalidPayment.setPaymentTimestamp(LocalDateTime.now());

        List<Payment> pendingPayments = Arrays.asList(validPayment, invalidPayment);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(paymentRepository.findByPatient_PatientIdAndStatus(1L, PaymentStatus.Pending))
                .thenReturn(pendingPayments);

        // When
        List<PaymentDTO> result = paymentService.getPendingPayments(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2); // Both payments should be processed, even with errors
    }

    @Test
    @DisplayName("makePayment - generates unique transaction ID")
    void makePayment_generatesUniqueTransactionId() {
        // Given
        when(patientRepository.findByPatientId(1L)).thenReturn(Optional.of(testPatient));
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(testAppointment));
        when(cardPaymentStrategy.processPayment(any(PaymentDTO.class))).thenReturn(PaymentStatus.Paid);
        
        Payment savedPayment1 = new Payment();
        savedPayment1.setTransactionId("TXN-UNIQUE-1");
        Payment savedPayment2 = new Payment();
        savedPayment2.setTransactionId("TXN-UNIQUE-2");
        
        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(savedPayment1)
                .thenReturn(savedPayment2);

        // When
        PaymentDTO result1 = paymentService.makePayment(validPaymentDTO);
        PaymentDTO result2 = paymentService.makePayment(validPaymentDTO);

        // Then
        assertThat(result1.getTransactionId()).isNotEqualTo(result2.getTransactionId());
        assertThat(result1.getTransactionId()).isEqualTo("TXN-UNIQUE-1");
        assertThat(result2.getTransactionId()).isEqualTo("TXN-UNIQUE-2");
    }

    @Test
    @DisplayName("makePayment - sets correct payment timestamp")
    void makePayment_setsCorrectTimestamp() {
        // Given
        LocalDateTime beforeCall = LocalDateTime.now();
        when(patientRepository.findByPatientId(1L)).thenReturn(Optional.of(testPatient));
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(testAppointment));
        when(cardPaymentStrategy.processPayment(any(PaymentDTO.class))).thenReturn(PaymentStatus.Paid);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            assertThat(payment.getPaymentTimestamp()).isNotNull();
            assertThat(payment.getPaymentTimestamp()).isAfterOrEqualTo(beforeCall);
            assertThat(payment.getPaymentTimestamp()).isBeforeOrEqualTo(LocalDateTime.now());
            return testPayment;
        });

        // When
        paymentService.makePayment(validPaymentDTO);

        // Then
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("makePayment - handles null payment method gracefully")
    void makePayment_nullPaymentMethod_throwsException() {
        // Given
        PaymentDTO nullPaymentMethodDTO = new PaymentDTO();
        nullPaymentMethodDTO.setPatientId(1L);
        nullPaymentMethodDTO.setAppointmentId(100L);
        nullPaymentMethodDTO.setAmount(1500.00);
        nullPaymentMethodDTO.setPaymentMethod(null);

        // When & Then
        assertThatThrownBy(() -> paymentService.makePayment(nullPaymentMethodDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid payment method: null");
    }

    @Test
    @DisplayName("makePayment - handles empty payment method gracefully")
    void makePayment_emptyPaymentMethod_throwsException() {
        // Given
        PaymentDTO emptyPaymentMethodDTO = new PaymentDTO();
        emptyPaymentMethodDTO.setPatientId(1L);
        emptyPaymentMethodDTO.setAppointmentId(100L);
        emptyPaymentMethodDTO.setAmount(1500.00);
        emptyPaymentMethodDTO.setPaymentMethod("");

        // When & Then
        assertThatThrownBy(() -> paymentService.makePayment(emptyPaymentMethodDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid payment method: ");
    }

    @Test
    @DisplayName("getPendingPayments - handles null patient ID gracefully")
    void getPendingPayments_nullPatientId_returnsEmptyList() {
        // When
        List<PaymentDTO> result = paymentService.getPendingPayments(null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getPendingPayments - handles negative patient ID gracefully")
    void getPendingPayments_negativePatientId_returnsEmptyList() {
        // Given
        when(patientRepository.findById(-1L)).thenReturn(Optional.empty());
        when(patientRepository.findByPatientId(-1L)).thenReturn(Optional.empty());

        // When
        List<PaymentDTO> result = paymentService.getPendingPayments(-1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("makePayment - handles zero amount payment")
    void makePayment_zeroAmount_success() {
        // Given
        PaymentDTO zeroAmountPayment = new PaymentDTO();
        zeroAmountPayment.setPatientId(1L);
        zeroAmountPayment.setAppointmentId(100L);
        zeroAmountPayment.setAmount(0.0);
        zeroAmountPayment.setPaymentMethod("Cash");

        when(patientRepository.findByPatientId(1L)).thenReturn(Optional.of(testPatient));
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(testAppointment));
        when(cashPaymentStrategy.processPayment(any(PaymentDTO.class))).thenReturn(PaymentStatus.Paid);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        PaymentDTO result = paymentService.makePayment(zeroAmountPayment);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualTo(0.0);
        assertThat(result.getStatus()).isEqualTo("Paid");

        verify(cashPaymentStrategy).processPayment(zeroAmountPayment);
    }

    @Test
    @DisplayName("makePayment - handles very large amount payment")
    void makePayment_largeAmount_success() {
        // Given
        PaymentDTO largeAmountPayment = new PaymentDTO();
        largeAmountPayment.setPatientId(1L);
        largeAmountPayment.setAppointmentId(100L);
        largeAmountPayment.setAmount(999999.99);
        largeAmountPayment.setPaymentMethod("Card");

        when(patientRepository.findByPatientId(1L)).thenReturn(Optional.of(testPatient));
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(testAppointment));
        when(cardPaymentStrategy.processPayment(any(PaymentDTO.class))).thenReturn(PaymentStatus.Paid);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        PaymentDTO result = paymentService.makePayment(largeAmountPayment);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualTo(999999.99);
        assertThat(result.getStatus()).isEqualTo("Paid");

        verify(cardPaymentStrategy).processPayment(largeAmountPayment);
    }
}
