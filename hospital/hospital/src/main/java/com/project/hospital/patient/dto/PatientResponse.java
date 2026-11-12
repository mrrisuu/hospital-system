package com.project.hospital.patient.dto;

import com.project.hospital.patient.entity.Gender;

import java.time.LocalDate;

public record PatientResponse(
        Long id,
        String medicalRecordNumber,
        String email,
        String fullName,
        LocalDate birthDate,
        Gender gender,
        String phone,
        String address
) {}