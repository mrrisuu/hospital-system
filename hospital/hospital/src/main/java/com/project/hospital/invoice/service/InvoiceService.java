package com.project.hospital.invoice.service;

import com.project.hospital.common.dto.PageResponse;
import com.project.hospital.common.exception.BusinessRuleViolationException;
import com.project.hospital.common.exception.ResourceConflictException;
import com.project.hospital.common.exception.ResourceNotFoundException;
import com.project.hospital.invoice.dto.InvoiceCreateRequest;
import com.project.hospital.invoice.dto.InvoiceItemRequest;
import com.project.hospital.invoice.dto.InvoiceResponse;
import com.project.hospital.invoice.dto.InvoiceSummaryResponse;
import com.project.hospital.invoice.entity.Invoice;
import com.project.hospital.invoice.entity.InvoiceItem;
import com.project.hospital.invoice.entity.InvoiceStatus;
import com.project.hospital.invoice.mapper.InvoiceMapper;
import com.project.hospital.invoice.repository.InvoiceRepository;
import com.project.hospital.visit.entity.Visit;
import com.project.hospital.visit.entity.VisitStatus;
import com.project.hospital.visit.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final VisitRepository visitRepository;
    private final InvoiceMapper invoiceMapper;

    @Transactional
    public InvoiceResponse create(InvoiceCreateRequest request) {
        Visit visit = visitRepository.findById(request.visitId())
                .orElseThrow(() -> new ResourceNotFoundException("Visit not found"));

        if (visit.getStatus() != VisitStatus.COMPLETED) {
            throw new BusinessRuleViolationException(
                    "Invoice can only be created for a completed visit");
        }
        if (invoiceRepository.existsByVisitId(visit.getId())) {
            throw new ResourceConflictException("Invoice already exists for this visit");
        }

        Invoice invoice = new Invoice();
        invoice.setVisit(visit);
        invoice.setInvoiceNumber(
                String.format("INV%06d", invoiceRepository.nextInvoiceSequence()));
        invoice.setStatus(InvoiceStatus.UNPAID);
        invoice.setPaidAmount(BigDecimal.ZERO.setScale(2));

        // total dihitung server dari item, tidak pernah dari klien
        BigDecimal total = BigDecimal.ZERO;
        for (InvoiceItemRequest itemRequest : request.items()) {
            InvoiceItem item = new InvoiceItem();
            item.setDescription(itemRequest.description().trim());
            item.setQuantity(itemRequest.quantity());
            item.setUnitPrice(itemRequest.unitPrice().setScale(2, RoundingMode.HALF_UP));
            invoice.addItem(item);

            total = total.add(item.getSubtotal());
        }
        invoice.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));

        return invoiceMapper.toResponse(invoiceRepository.save(invoice));
    }

    @Transactional(readOnly = true)
    public PageResponse<InvoiceSummaryResponse> getAll(InvoiceStatus status, int page, int size) {
        Pageable pageable = pageable(page, size);

        Page<Invoice> result = (status == null)
                ? invoiceRepository.findAll(pageable)
                : invoiceRepository.findByStatus(status, pageable);

        return PageResponse.from(result.map(invoiceMapper::toSummary));
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        return invoiceMapper.toResponse(invoice);
    }

    @Transactional(readOnly = true)
    public PageResponse<InvoiceSummaryResponse> getMine(String email, int page, int size) {
        Page<Invoice> result = invoiceRepository
                .findByVisitPatientUserEmail(email, pageable(page, size));

        return PageResponse.from(result.map(invoiceMapper::toSummary));
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getMineById(String email, Long id) {
        // invoice milik pasien lain dianggap "tidak ditemukan"
        Invoice invoice = invoiceRepository.findByIdAndVisitPatientUserEmail(id, email)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        return invoiceMapper.toResponse(invoice);
    }

    private Pageable pageable(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessRuleViolationException(
                    "Page must be at least 0 and size must be between 1 and 100");
        }
        return PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
    }
}