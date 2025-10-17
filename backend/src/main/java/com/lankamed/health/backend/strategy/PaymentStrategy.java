package com.lankamed.health.backend.strategy;

import com.lankamed.health.backend.dto.PaymentDTO;
import com.lankamed.health.backend.model.PaymentStatus;

public interface PaymentStrategy {
    PaymentStatus processPayment(PaymentDTO paymentDTO);
}
