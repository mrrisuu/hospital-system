package com.project.hospital.invoice.repository;

import com.project.hospital.invoice.entity.Invoice;
import com.project.hospital.invoice.entity.InvoiceStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    // detail: ikut memuat items (satu hasil, tanpa pagination)
    @Override
    @EntityGraph(attributePaths = {"items", "visit", "visit.patient",
            "visit.doctor", "visit.doctor.poli"})
    Optional<Invoice> findById(Long id);

    @EntityGraph(attributePaths = {"items", "visit", "visit.patient",
            "visit.doctor", "visit.doctor.poli"})
    Optional<Invoice> findByIdAndVisitPatientUserEmail(Long id, String email);

    // daftar: tanpa items, supaya pagination tetap dilakukan di database
    @Override
    @EntityGraph(attributePaths = {"visit", "visit.patient", "visit.doctor", "visit.doctor.poli"})
    Page<Invoice> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"visit", "visit.patient", "visit.doctor", "visit.doctor.poli"})
    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"visit", "visit.patient", "visit.doctor", "visit.doctor.poli"})
    Page<Invoice> findByVisitPatientUserEmail(String email, Pageable pageable);

    boolean existsByVisitId(Long visitId);

    // dikunci saat pembayaran, supaya dua pembayaran bersamaan tidak melewati total
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Invoice i where i.id = :id")
    Optional<Invoice> findByIdForUpdate(@Param("id") Long id);

    @Query(value = "SELECT nextval('invoice_seq')", nativeQuery = true)
    Long nextInvoiceSequence();
}