package com.project.hospital.doctor.dto;

import jakarta.validation.constraints.*;

public record DoctorCreateRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email format is invalid")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be 8 to 100 characters")
        String password,

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