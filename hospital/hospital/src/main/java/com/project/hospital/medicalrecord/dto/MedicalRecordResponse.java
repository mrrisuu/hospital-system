package com.project.hospital.medicalrecord.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MedicalRecordResponse(
        Long id,
        Long visitId,
        LocalDate visitDate,
        Long patientId,
        String patientName,
        String medicalRecordNumber,
        Long doctorId,
        String doctorName,
        String poliName,
        String symptoms,
        String diagnosis,
        String treatment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}