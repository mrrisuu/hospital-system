package com.project.hospital.invoice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import com.project.hospital.invoice.entity.PaymentMethod;

import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Amount must have at most 2 decimal places")
        BigDecimal amount,

        @NotNull(message = "Payment method is required")
        PaymentMethod method
) {}