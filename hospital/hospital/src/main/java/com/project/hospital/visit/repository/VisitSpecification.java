package com.project.hospital.visit.repository;

import com.project.hospital.visit.entity.Visit;
import com.project.hospital.visit.entity.VisitStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class VisitSpecification {

    private VisitSpecification() {
    }

    public static Specification<Visit> filter(Long doctorId, LocalDate visitDate, VisitStatus status) {
        return (root, _, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (doctorId != null) {
                predicates.add(cb.equal(root.get("doctor").get("id"), doctorId));
            }
            if (visitDate != null) {
                predicates.add(cb.equal(root.get("visitDate"), visitDate));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}