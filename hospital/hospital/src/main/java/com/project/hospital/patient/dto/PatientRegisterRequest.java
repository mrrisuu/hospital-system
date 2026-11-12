package com.project.hospital.patient.dto;

import com.project.hospital.patient.entity.Gender;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record PatientRegisterRequest(

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

        @NotNull(message = "Birth date is required")
        @Past(message = "Birth date must be in the past")
        LocalDate birthDate,

        @NotNull(message = "Gender is required")
        Gender gender,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[0-9]{10,20}$", message = "Phone number must contain 10 to 20 digits")
        String phone,

        @Size(max = 255, message = "Address must not exceed 255 characters")
        String address
) {}