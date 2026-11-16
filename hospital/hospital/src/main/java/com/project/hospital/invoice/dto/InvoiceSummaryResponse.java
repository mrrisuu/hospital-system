package com.project.hospital.invoice.dto;

import com.project.hospital.invoice.entity.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record InvoiceSummaryResponse(
        Long id,
        String invoiceNumber,
        Long visitId,
        LocalDate visitDate,
        Long patientId,
        String patientName,
        String medicalRecordNumber,
        String doctorName,
        String poliName,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal remainingAmount,
        InvoiceStatus status,
        LocalDateTime createdAt
) {}