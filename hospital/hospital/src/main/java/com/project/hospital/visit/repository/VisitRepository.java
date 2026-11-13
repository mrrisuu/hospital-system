package com.project.hospital.visit.repository;

import com.project.hospital.visit.entity.Visit;
import com.project.hospital.visit.entity.VisitStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface VisitRepository
        extends JpaRepository<Visit, Long>, JpaSpecificationExecutor<Visit> {

    @Override
    @EntityGraph(attributePaths = {"patient", "doctor", "doctor.poli", "schedule"})
    Page<Visit> findAll(Specification<Visit> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"patient", "doctor", "doctor.poli", "schedule"})
    Optional<Visit> findById(Long id);

    @EntityGraph(attributePaths = {"patient", "doctor", "doctor.poli", "schedule"})
    Page<Visit> findByPatientUserEmail(String email, Pageable pageable);

    @EntityGraph(attributePaths = {"patient", "doctor", "doctor.poli", "schedule"})
    Optional<Visit> findByIdAndPatientUserEmail(Long id, String email);

    boolean existsByPatientIdAndDoctorIdAndVisitDateAndStatusNot(
            Long patientId, Long doctorId, LocalDate visitDate, VisitStatus status);

    long countByScheduleIdAndVisitDateAndStatusNot(
            Long scheduleId, LocalDate visitDate, VisitStatus status);

    @Query("""
            select coalesce(max(v.queueNumber), 0) from Visit v
            where v.doctor.id = :doctorId and v.visitDate = :visitDate
            """)
    int findMaxQueueNumber(@Param("doctorId") Long doctorId,
                           @Param("visitDate") LocalDate visitDate);
}