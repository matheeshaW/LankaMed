package com.lankamed.health.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ParseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lankamed.health.backend.dto.PaymentDTO;
import com.lankamed.health.backend.service.PaymentService;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/pending/{patientId}")
    public List<PaymentDTO> getPendingPayments(@PathVariable Long patientId) {
        return paymentService.getPendingPayments(patientId);
    }

    @PostMapping("/make")
    public PaymentDTO makePayment(@RequestBody PaymentDTO paymentDTO) throws ParseException {
        return paymentService.makePayment(paymentDTO);
    }
}

