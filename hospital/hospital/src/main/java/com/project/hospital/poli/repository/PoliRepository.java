package com.project.hospital.poli.repository;

import com.project.hospital.poli.entity.Poli;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PoliRepository extends JpaRepository<Poli, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}