package com.lankamed.health.backend.service;

import java.util.List;

import com.lankamed.health.backend.dto.PaymentDTO;

public interface PaymentService {
    PaymentDTO makePayment(PaymentDTO paymentDTO);
    List<PaymentDTO> getPendingPayments(Long patientId);
}
