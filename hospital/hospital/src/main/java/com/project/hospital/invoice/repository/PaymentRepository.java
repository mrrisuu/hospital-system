package com.project.hospital.invoice.repository;

import com.project.hospital.invoice.entity.Payment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @EntityGraph(attributePaths = "receivedBy")
    List<Payment> findByInvoiceIdOrderByPaidAtAscIdAsc(Long invoiceId);
}