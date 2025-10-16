package com.lankamed.health.backend.strategy;

import org.springframework.stereotype.Service;

import com.lankamed.health.backend.dto.PaymentDTO;
import com.lankamed.health.backend.model.PaymentStatus;

@Service
public class InsurancePayment implements PaymentStrategy {
    @Override
    public PaymentStatus processPayment(PaymentDTO dto) {
        // call insurance API, validate
        boolean approved = true; // simulate
        return approved ? PaymentStatus.Paid : PaymentStatus.Failed;
    }
}

