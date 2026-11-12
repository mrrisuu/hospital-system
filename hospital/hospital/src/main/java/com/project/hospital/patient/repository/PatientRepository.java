package com.project.hospital.patient.repository;

import com.project.hospital.patient.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    @EntityGraph(attributePaths = "user")
    Optional<Patient> findByUserEmail(String email);

    @EntityGraph(attributePaths = "user")
    Page<Patient> findByFullNameContainingIgnoreCase(String name, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "user")
    Page<Patient> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "user")
    Optional<Patient> findById(Long id);

    @Query(value = "SELECT nextval('medical_record_seq')", nativeQuery = true)
    Long nextMedicalRecordSequence();
}