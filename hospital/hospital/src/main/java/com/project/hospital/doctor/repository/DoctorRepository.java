package com.project.hospital.doctor.repository;

import com.project.hospital.doctor.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface DoctorRepository
        extends JpaRepository<Doctor, Long>, JpaSpecificationExecutor<Doctor> {

    @Override
    @EntityGraph(attributePaths = {"user", "poli"})
    Page<Doctor> findAll(Specification<Doctor> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"user", "poli"})
    Optional<Doctor> findById(Long id);

    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByLicenseNumberAndIdNot(String licenseNumber, Long id);
}