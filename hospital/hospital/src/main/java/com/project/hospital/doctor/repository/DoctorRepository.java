package com.project.hospital.doctor.repository;

import com.project.hospital.doctor.entity.Doctor;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Doctor d where d.id = :id")
    Optional<Doctor> findByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = {"user", "poli"})
    Optional<Doctor> findByUserEmail(String email);
}