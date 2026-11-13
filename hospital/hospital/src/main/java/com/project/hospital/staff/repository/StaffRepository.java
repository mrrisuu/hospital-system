package com.project.hospital.staff.repository;

import com.project.hospital.staff.entity.Staff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    @EntityGraph(attributePaths = "user")
    Page<Staff> findByFullNameContainingIgnoreCase(String name, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "user")
    Page<Staff> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "user")
    Optional<Staff> findById(Long id);


}
