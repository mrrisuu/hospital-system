package com.project.hospital.medicalrecord.repository;

import com.project.hospital.medicalrecord.entity.MedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    @EntityGraph(attributePaths = {"visit", "visit.patient", "visit.doctor", "visit.doctor.poli"})
    Optional<MedicalRecord> findByIdAndVisitDoctorUserEmail(Long id, String email);

    @EntityGraph(attributePaths = {"visit", "visit.patient", "visit.doctor", "visit.doctor.poli"})
    Page<MedicalRecord> findByVisitPatientUserEmail(String email, Pageable pageable);

    @EntityGraph(attributePaths = {"visit", "visit.patient", "visit.doctor", "visit.doctor.poli"})
    Page<MedicalRecord> findByVisitPatientId(Long patientId, Pageable pageable);

    boolean existsByVisitId(Long visitId);
}