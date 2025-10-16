package com.lankamed.health.backend.strategy;

import org.springframework.stereotype.Service;

import com.lankamed.health.backend.dto.PaymentDTO;
import com.lankamed.health.backend.model.PaymentStatus;

@Service
public class CardPayment implements PaymentStrategy {
    @Override
    public PaymentStatus processPayment(PaymentDTO dto) {
        // simulate or integrate payment gateway
        boolean success = true; 
        return success ? PaymentStatus.Paid : PaymentStatus.Failed;
    }
}

