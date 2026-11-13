package com.project.hospital.visit.dto;

import com.project.hospital.visit.entity.VisitStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record VisitResponse(
        Long id,
        Long patientId,
        String patientName,
        String medicalRecordNumber,
        Long doctorId,
        String doctorName,
        String poliName,
        Long scheduleId,
        LocalDate visitDate,
        LocalTime startTime,
        LocalTime endTime,
        Integer queueNumber,
        VisitStatus status,
        String complaint
) {}