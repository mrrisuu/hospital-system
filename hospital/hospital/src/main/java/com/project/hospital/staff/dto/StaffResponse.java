package com.project.hospital.staff.dto;

public record StaffResponse(
        Long id,
        String email,
        String fullName,
        String phone
) {
}
