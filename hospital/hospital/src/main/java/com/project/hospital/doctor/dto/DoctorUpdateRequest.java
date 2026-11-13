package com.project.hospital.doctor.dto;

import jakarta.validation.constraints.*;

public record DoctorUpdateRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 100, message = "Full name must not exceed 100 characters")
        String fullName,

        @NotBlank(message = "License number is required")
        @Size(max = 50, message = "License number must not exceed 50 characters")
        String licenseNumber,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[0-9]{10,20}$", message = "Phone number must contain 10 to 20 digits")
        String phone,

        @NotNull(message = "Poli ID is required")
        @Positive(message = "Poli ID must be greater than 0")
        Long poliId
) {}