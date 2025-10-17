package com.lankamed.health.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lankamed.health.backend.dto.PaymentDTO;
import com.lankamed.health.backend.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PaymentController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                com.lankamed.health.backend.config.SecurityConfig.class,
                com.lankamed.health.backend.security.JwtAuthenticationFilter.class
        }))
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    private PaymentDTO validPaymentDTO;
    private PaymentDTO responsePaymentDTO;
    private List<PaymentDTO> pendingPayments;

    @BeforeEach
    void setUp() {
        // Setup valid payment DTO for requests
        validPaymentDTO = new PaymentDTO();
        validPaymentDTO.setPatientId(1L);
        validPaymentDTO.setAppointmentId(100L);
        validPaymentDTO.setAmount(1500.00);
        validPaymentDTO.setPaymentMethod("Card");
        validPaymentDTO.setCardNumber("1234567890123456");

        // Setup response payment DTO
        responsePaymentDTO = new PaymentDTO();
        responsePaymentDTO.setPatientId(1L);
        responsePaymentDTO.setAppointmentId(100L);
        responsePaymentDTO.setAmount(1500.00);
        responsePaymentDTO.setPaymentMethod("Card");
        responsePaymentDTO.setTransactionId("TXN-123456789");
        responsePaymentDTO.setStatus("Paid");

        // Setup pending payments list
        PaymentDTO pendingPayment1 = new PaymentDTO();
        pendingPayment1.setPatientId(1L);
        pendingPayment1.setAppointmentId(100L);
        pendingPayment1.setAmount(1500.00);
        pendingPayment1.setPaymentMethod("Card");
        pendingPayment1.setTransactionId("TXN-PENDING-001");
        pendingPayment1.setStatus("Pending");
        pendingPayment1.setAppointmentDateTime("2024-01-15T10:00:00");
        pendingPayment1.setDoctorName("Dr. John Smith");
        pendingPayment1.setServiceName("General Consultation");
        pendingPayment1.setHospitalName("City Hospital");

        PaymentDTO pendingPayment2 = new PaymentDTO();
        pendingPayment2.setPatientId(1L);
        pendingPayment2.setAppointmentId(101L);
        pendingPayment2.setAmount(2000.00);
        pendingPayment2.setPaymentMethod("Insurance");
        pendingPayment2.setTransactionId("TXN-PENDING-002");
        pendingPayment2.setStatus("Pending");
        pendingPayment2.setAppointmentDateTime("2024-01-16T14:30:00");
        pendingPayment2.setDoctorName("Dr. Jane Doe");
        pendingPayment2.setServiceName("Specialist Consultation");
        pendingPayment2.setHospitalName("Medical Center");

        pendingPayments = Arrays.asList(pendingPayment1, pendingPayment2);
    }

    @Test
    @DisplayName("GET /payments/pending/{patientId} - returns pending payments successfully")
    void getPendingPayments_success() throws Exception {
        // Given
        Long patientId = 1L;
        when(paymentService.getPendingPayments(patientId)).thenReturn(pendingPayments);

        // When & Then
        mockMvc.perform(get("/payments/pending/{patientId}", patientId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].patientId", is(1)))
                .andExpect(jsonPath("$[0].appointmentId", is(100)))
                .andExpect(jsonPath("$[0].amount", is(1500.0)))
                .andExpect(jsonPath("$[0].paymentMethod", is("Card")))
                .andExpect(jsonPath("$[0].transactionId", is("TXN-PENDING-001")))
                .andExpect(jsonPath("$[0].status", is("Pending")))
                .andExpect(jsonPath("$[0].appointmentDateTime", is("2024-01-15T10:00:00")))
                .andExpect(jsonPath("$[0].doctorName", is("Dr. John Smith")))
                .andExpect(jsonPath("$[0].serviceName", is("General Consultation")))
                .andExpect(jsonPath("$[0].hospitalName", is("City Hospital")))
                .andExpect(jsonPath("$[1].patientId", is(1)))
                .andExpect(jsonPath("$[1].appointmentId", is(101)))
                .andExpect(jsonPath("$[1].amount", is(2000.0)))
                .andExpect(jsonPath("$[1].paymentMethod", is("Insurance")))
                .andExpect(jsonPath("$[1].transactionId", is("TXN-PENDING-002")))
                .andExpect(jsonPath("$[1].status", is("Pending")));

        verify(paymentService, times(1)).getPendingPayments(patientId);
    }

    @Test
    @DisplayName("GET /payments/pending/{patientId} - returns empty list when no pending payments")
    void getPendingPayments_emptyList() throws Exception {
        // Given
        Long patientId = 1L;
        when(paymentService.getPendingPayments(patientId)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/payments/pending/{patientId}", patientId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(paymentService, times(1)).getPendingPayments(patientId);
    }

    @Test
    @DisplayName("GET /payments/pending/{patientId} - handles patient not found exception")
    void getPendingPayments_patientNotFound() throws Exception {
        // Given
        Long patientId = 999L;
        when(paymentService.getPendingPayments(patientId))
                .thenThrow(new IllegalArgumentException("Patient not found with ID: " + patientId));

        // When & Then
        mockMvc.perform(get("/payments/pending/{patientId}", patientId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", containsString("Patient not found")));

        verify(paymentService, times(1)).getPendingPayments(patientId);
    }

    @Test
    @DisplayName("GET /payments/pending/{patientId} - handles general service exception")
    void getPendingPayments_generalException() throws Exception {
        // Given
        Long patientId = 1L;
        when(paymentService.getPendingPayments(patientId))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/payments/pending/{patientId}", patientId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", containsString("Error fetching pending payments")));

        verify(paymentService, times(1)).getPendingPayments(patientId);
    }

    @Test
    @DisplayName("POST /payments/make - processes payment successfully")
    void makePayment_success() throws Exception {
        // Given
        when(paymentService.makePayment(any(PaymentDTO.class))).thenReturn(responsePaymentDTO);

        // When & Then
        mockMvc.perform(post("/payments/make")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPaymentDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.patientId", is(1)))
                .andExpect(jsonPath("$.appointmentId", is(100)))
                .andExpect(jsonPath("$.amount", is(1500.0)))
                .andExpect(jsonPath("$.paymentMethod", is("Card")))
                .andExpect(jsonPath("$.transactionId", is("TXN-123456789")))
                .andExpect(jsonPath("$.status", is("Paid")));

        verify(paymentService, times(1)).makePayment(any(PaymentDTO.class));
    }

    @Test
    @DisplayName("POST /payments/make - processes insurance payment successfully")
    void makePayment_insurancePayment_success() throws Exception {
        // Given
        PaymentDTO insurancePaymentDTO = new PaymentDTO();
        insurancePaymentDTO.setPatientId(1L);
        insurancePaymentDTO.setAppointmentId(100L);
        insurancePaymentDTO.setAmount(2000.00);
        insurancePaymentDTO.setPaymentMethod("Insurance");
        insurancePaymentDTO.setInsuranceNumber("INS-123456");

        PaymentDTO responseDTO = new PaymentDTO();
        responseDTO.setPatientId(1L);
        responseDTO.setAppointmentId(100L);
        responseDTO.setAmount(2000.00);
        responseDTO.setPaymentMethod("Insurance");
        responseDTO.setTransactionId("TXN-INS-789");
        responseDTO.setStatus("Paid");

        when(paymentService.makePayment(any(PaymentDTO.class))).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(post("/payments/make")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(insurancePaymentDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.paymentMethod", is("Insurance")))
                .andExpect(jsonPath("$.amount", is(2000.0)))
                .andExpect(jsonPath("$.transactionId", is("TXN-INS-789")))
                .andExpect(jsonPath("$.status", is("Paid")));

        verify(paymentService, times(1)).makePayment(any(PaymentDTO.class));
    }

    @Test
    @DisplayName("POST /payments/make - processes cash payment successfully")
    void makePayment_cashPayment_success() throws Exception {
        // Given
        PaymentDTO cashPaymentDTO = new PaymentDTO();
        cashPaymentDTO.setPatientId(1L);
        cashPaymentDTO.setAppointmentId(100L);
        cashPaymentDTO.setAmount(1000.00);
        cashPaymentDTO.setPaymentMethod("Cash");

        PaymentDTO responseDTO = new PaymentDTO();
        responseDTO.setPatientId(1L);
        responseDTO.setAppointmentId(100L);
        responseDTO.setAmount(1000.00);
        responseDTO.setPaymentMethod("Cash");
        responseDTO.setTransactionId("TXN-CASH-456");
        responseDTO.setStatus("Paid");

        when(paymentService.makePayment(any(PaymentDTO.class))).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(post("/payments/make")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cashPaymentDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.paymentMethod", is("Cash")))
                .andExpect(jsonPath("$.amount", is(1000.0)))
                .andExpect(jsonPath("$.transactionId", is("TXN-CASH-456")))
                .andExpect(jsonPath("$.status", is("Paid")));

        verify(paymentService, times(1)).makePayment(any(PaymentDTO.class));
    }

    @Test
    @DisplayName("POST /payments/make - handles invalid payment method")
    void makePayment_invalidPaymentMethod() throws Exception {
        // Given
        PaymentDTO invalidPaymentDTO = new PaymentDTO();
        invalidPaymentDTO.setPatientId(1L);
        invalidPaymentDTO.setAppointmentId(100L);
        invalidPaymentDTO.setAmount(1500.00);
        invalidPaymentDTO.setPaymentMethod("InvalidMethod");

        when(paymentService.makePayment(any(PaymentDTO.class)))
                .thenThrow(new IllegalArgumentException("Invalid payment method: InvalidMethod"));

        // When & Then
        mockMvc.perform(post("/payments/make")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPaymentDTO)))
                .andExpect(status().isBadRequest());

        verify(paymentService, times(1)).makePayment(any(PaymentDTO.class));
    }

    @Test
    @DisplayName("POST /payments/make - handles patient not found")
    void makePayment_patientNotFound() throws Exception {
        // Given
        PaymentDTO paymentWithInvalidPatient = new PaymentDTO();
        paymentWithInvalidPatient.setPatientId(999L);
        paymentWithInvalidPatient.setAppointmentId(100L);
        paymentWithInvalidPatient.setAmount(1500.00);
        paymentWithInvalidPatient.setPaymentMethod("Card");

        when(paymentService.makePayment(any(PaymentDTO.class)))
                .thenThrow(new IllegalArgumentException("Patient not found with ID: 999"));

        // When & Then
        mockMvc.perform(post("/payments/make")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentWithInvalidPatient)))
                .andExpect(status().isBadRequest());

        verify(paymentService, times(1)).makePayment(any(PaymentDTO.class));
    }

    @Test
    @DisplayName("POST /payments/make - handles appointment not found")
    void makePayment_appointmentNotFound() throws Exception {
        // Given
        PaymentDTO paymentWithInvalidAppointment = new PaymentDTO();
        paymentWithInvalidAppointment.setPatientId(1L);
        paymentWithInvalidAppointment.setAppointmentId(999L);
        paymentWithInvalidAppointment.setAmount(1500.00);
        paymentWithInvalidAppointment.setPaymentMethod("Card");

        when(paymentService.makePayment(any(PaymentDTO.class)))
                .thenThrow(new IllegalArgumentException("Appointment not found with ID: 999"));

        // When & Then
        mockMvc.perform(post("/payments/make")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentWithInvalidAppointment)))
                .andExpect(status().isBadRequest());

        verify(paymentService, times(1)).makePayment(any(PaymentDTO.class));
    }

    @Test
    @DisplayName("POST /payments/make - handles parse exception")
    void makePayment_parseException() throws Exception {
        // Given
        when(paymentService.makePayment(any(PaymentDTO.class)))
                .thenThrow(new org.springframework.expression.ParseException(0, "Parse error"));

        // When & Then
        mockMvc.perform(post("/payments/make")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPaymentDTO)))
                .andExpect(status().isInternalServerError());

        verify(paymentService, times(1)).makePayment(any(PaymentDTO.class));
    }

    @Test
    @DisplayName("POST /payments/make - handles malformed JSON")
    void makePayment_malformedJson() throws Exception {
        // Given
        String malformedJson = "{ \"patientId\": 1, \"amount\": 1500.00, \"paymentMethod\": \"Card\""; // Missing closing brace

        // When & Then
        mockMvc.perform(post("/payments/make")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest());

        verify(paymentService, never()).makePayment(any(PaymentDTO.class));
    }

    @Test
    @DisplayName("POST /payments/make - handles missing required fields")
    void makePayment_missingRequiredFields() throws Exception {
        // Given
        PaymentDTO incompletePaymentDTO = new PaymentDTO();
        incompletePaymentDTO.setPatientId(1L);
        // Missing amount and paymentMethod

        // When & Then
        mockMvc.perform(post("/payments/make")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incompletePaymentDTO)))
                .andExpect(status().isOk()); // Controller doesn't validate, service will handle

        verify(paymentService, times(1)).makePayment(any(PaymentDTO.class));
    }

    @Test
    @DisplayName("GET /payments/pending/{patientId} - handles null patient ID")
    void getPendingPayments_nullPatientId() throws Exception {
        // Given
        when(paymentService.getPendingPayments(null))
                .thenThrow(new IllegalArgumentException("Patient ID cannot be null"));

        // When & Then
        mockMvc.perform(get("/payments/pending/{patientId}", (Object) null)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Spring will return 404 for null path variable

        verify(paymentService, never()).getPendingPayments(any());
    }

    @Test
    @DisplayName("GET /payments/pending/{patientId} - handles negative patient ID")
    void getPendingPayments_negativePatientId() throws Exception {
        // Given
        Long negativePatientId = -1L;
        when(paymentService.getPendingPayments(negativePatientId))
                .thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/payments/pending/{patientId}", negativePatientId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(paymentService, times(1)).getPendingPayments(negativePatientId);
    }

    @Test
    @DisplayName("POST /payments/make - handles zero amount payment")
    void makePayment_zeroAmount() throws Exception {
        // Given
        PaymentDTO zeroAmountPayment = new PaymentDTO();
        zeroAmountPayment.setPatientId(1L);
        zeroAmountPayment.setAppointmentId(100L);
        zeroAmountPayment.setAmount(0.0);
        zeroAmountPayment.setPaymentMethod("Cash");

        PaymentDTO responseDTO = new PaymentDTO();
        responseDTO.setPatientId(1L);
        responseDTO.setAppointmentId(100L);
        responseDTO.setAmount(0.0);
        responseDTO.setPaymentMethod("Cash");
        responseDTO.setTransactionId("TXN-ZERO-123");
        responseDTO.setStatus("Paid");

        when(paymentService.makePayment(any(PaymentDTO.class))).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(post("/payments/make")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zeroAmountPayment)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.amount", is(0.0)))
                .andExpect(jsonPath("$.status", is("Paid")));

        verify(paymentService, times(1)).makePayment(any(PaymentDTO.class));
    }

    @Test
    @DisplayName("POST /payments/make - handles very large amount payment")
    void makePayment_largeAmount() throws Exception {
        // Given
        PaymentDTO largeAmountPayment = new PaymentDTO();
        largeAmountPayment.setPatientId(1L);
        largeAmountPayment.setAppointmentId(100L);
        largeAmountPayment.setAmount(999999.99);
        largeAmountPayment.setPaymentMethod("Card");

        PaymentDTO responseDTO = new PaymentDTO();
        responseDTO.setPatientId(1L);
        responseDTO.setAppointmentId(100L);
        responseDTO.setAmount(999999.99);
        responseDTO.setPaymentMethod("Card");
        responseDTO.setTransactionId("TXN-LARGE-456");
        responseDTO.setStatus("Paid");

        when(paymentService.makePayment(any(PaymentDTO.class))).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(post("/payments/make")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(largeAmountPayment)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.amount", is(999999.99)))
                .andExpect(jsonPath("$.status", is("Paid")));

        verify(paymentService, times(1)).makePayment(any(PaymentDTO.class));
    }
}
