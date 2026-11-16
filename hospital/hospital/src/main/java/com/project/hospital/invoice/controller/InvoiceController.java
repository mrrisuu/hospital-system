package com.project.hospital.invoice.controller;

import com.project.hospital.common.dto.PageResponse;
import com.project.hospital.invoice.dto.InvoiceCreateRequest;
import com.project.hospital.invoice.dto.InvoiceResponse;
import com.project.hospital.invoice.dto.InvoiceSummaryResponse;
import com.project.hospital.invoice.dto.PaymentRequest;
import com.project.hospital.invoice.dto.PaymentResponse;
import com.project.hospital.invoice.entity.InvoiceStatus;
import com.project.hospital.invoice.service.InvoiceService;
import com.project.hospital.invoice.service.PaymentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final PaymentService paymentService;

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping
    public ResponseEntity<InvoiceResponse> create(
            @Valid @RequestBody InvoiceCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.create(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping
    public ResponseEntity<PageResponse<InvoiceSummaryResponse>> getAll(
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(invoiceService.getAll(status, page, size));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getById(id));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/me")
    public ResponseEntity<PageResponse<InvoiceSummaryResponse>> getMine(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(invoiceService.getMine(authentication.getName(), page, size));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/me/{id}")
    public ResponseEntity<InvoiceResponse> getMineById(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(invoiceService.getMineById(authentication.getName(), id));
    }

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/{id}/payments")
    public ResponseEntity<PaymentResponse> pay(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody PaymentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.pay(authentication.getName(), id, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/{id}/payments")
    public ResponseEntity<List<PaymentResponse>> getPayments(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getByInvoice(id));
    }
}