package com.project.hospital.medicalrecord.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MedicalRecordUpdateRequest(
        @NotBlank(message = "Symptoms are required")
        @Size(max = 500, message = "Symptoms must not exceed 500 characters")
        String symptoms,

        @NotBlank(message = "Diagnosis is required")
        @Size(max = 1000, message = "Diagnosis must not exceed 1000 characters")
        String diagnosis,

        @Size(max = 1000, message = "Treatment must not exceed 1000 characters")
        String treatment
) {}