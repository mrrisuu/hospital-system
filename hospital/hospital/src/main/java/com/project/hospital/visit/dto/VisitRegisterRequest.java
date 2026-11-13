package com.project.hospital.visit.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record VisitRegisterRequest(
        @NotNull(message = "Schedule ID is required")
        @Positive(message = "Schedule ID must be greater than 0")
        Long scheduleId,

        @NotNull(message = "Visit date is required")
        LocalDate visitDate,

        @Size(max = 500, message = "Complaint must not exceed 500 characters")
        String complaint
) {}