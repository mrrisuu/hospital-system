package com.project.hospital.invoice.dto;

import com.project.hospital.invoice.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long invoiceId,
        BigDecimal amount,
        PaymentMethod method,
        String receivedBy,
        LocalDateTime paidAt
) {}