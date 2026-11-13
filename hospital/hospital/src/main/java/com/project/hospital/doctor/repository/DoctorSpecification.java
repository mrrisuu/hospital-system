package com.project.hospital.doctor.repository;

import com.project.hospital.doctor.entity.Doctor;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class DoctorSpecification {

    private DoctorSpecification() {
    }

    //Ini namanya lambda expression (ekspresi lambda), yaitu cara singkat menulis sebuah method tanpa nama.
    public static Specification<Doctor> filter(Long poliId, String name) {//filter(...) adalah method biasa yang mengembalikan sebuah Specification<Doctor>.
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (poliId != null) {
                predicates.add(cb.equal(root.get("poli").get("id"), poliId));
            }
            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("fullName")),
                        "%" + name.trim().toLowerCase() + "%"
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}