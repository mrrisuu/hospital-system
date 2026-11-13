package com.project.hospital.doctor.dto;

public record DoctorResponse(
        Long id,
        String email,
        String fullName,
        String licenseNumber,
        String phone,
        Long poliId,
        String poliName
) {}