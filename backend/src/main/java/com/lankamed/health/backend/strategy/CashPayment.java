package com.lankamed.health.backend.strategy;

import org.springframework.stereotype.Service;

import com.lankamed.health.backend.dto.PaymentDTO;
import com.lankamed.health.backend.model.PaymentStatus;

@Service
public class CashPayment implements PaymentStrategy {
    @Override
    public PaymentStatus processPayment(PaymentDTO dto) {
        return PaymentStatus.Pending; // Cash requires staff confirmation
    }
}

