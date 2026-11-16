package com.project.hospital.invoice.service;

import com.project.hospital.common.exception.BusinessRuleViolationException;
import com.project.hospital.common.exception.ResourceNotFoundException;
import com.project.hospital.invoice.dto.PaymentRequest;
import com.project.hospital.invoice.dto.PaymentResponse;
import com.project.hospital.invoice.entity.Invoice;
import com.project.hospital.invoice.entity.InvoiceStatus;
import com.project.hospital.invoice.entity.Payment;
import com.project.hospital.invoice.mapper.InvoiceMapper;
import com.project.hospital.invoice.repository.InvoiceRepository;
import com.project.hospital.invoice.repository.PaymentRepository;
import com.project.hospital.user.entity.User;
import com.project.hospital.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final InvoiceMapper invoiceMapper;

    @Transactional
    public PaymentResponse pay(String staffEmail, Long invoiceId, PaymentRequest request) {
        Invoice invoice = invoiceRepository.findByIdForUpdate(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BusinessRuleViolationException("Invoice is already fully paid");
        }

        var amount = request.amount().setScale(2, RoundingMode.HALF_UP);

        if (amount.compareTo(invoice.getRemainingAmount()) > 0) {
            throw new BusinessRuleViolationException(
                    "Payment amount exceeds the remaining amount");
        }

        User staff = userRepository.findByEmail(staffEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmount(amount);
        payment.setMethod(request.method());
        payment.setReceivedBy(staff);

        invoice.setPaidAmount(invoice.getPaidAmount().add(amount));
        invoice.setStatus(
                invoice.getPaidAmount().compareTo(invoice.getTotalAmount()) == 0
                        ? InvoiceStatus.PAID
                        : InvoiceStatus.PARTIALLY_PAID);
        invoiceRepository.save(invoice);

        return invoiceMapper.toPaymentResponse(paymentRepository.save(payment));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getByInvoice(Long invoiceId) {
        if (!invoiceRepository.existsById(invoiceId)) {
            throw new ResourceNotFoundException("Invoice not found");
        }

        return paymentRepository.findByInvoiceIdOrderByPaidAtAscIdAsc(invoiceId).stream()
                .map(invoiceMapper::toPaymentResponse)
                .toList();
    }
}