package com.lankamed.health.backend.strategy;

import com.lankamed.health.backend.dto.PaymentDTO;
import com.lankamed.health.backend.model.PaymentStatus;
import org.springframework.stereotype.Component;

/**
 * Mock payment strategy for testing purposes.
 * This strategy always returns a successful payment status.
 */
@Component
public class MockPaymentStrategy implements PaymentStrategy {

    private PaymentStatus returnStatus = PaymentStatus.Paid;

    @Override
    public PaymentStatus processPayment(PaymentDTO paymentDTO) {
        // Simulate some basic validation
        if (paymentDTO.getAmount() < 0) {
            return PaymentStatus.Failed;
        }
        
        if (paymentDTO.getAmount() > 100000) {
            return PaymentStatus.Failed;
        }
        
        return returnStatus;
    }

    /**
     * Set the status to return for testing purposes
     */
    public void setReturnStatus(PaymentStatus status) {
        this.returnStatus = status;
    }

    /**
     * Reset to default successful status
     */
    public void reset() {
        this.returnStatus = PaymentStatus.Paid;
    }
}
