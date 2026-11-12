package com.project.hospital.auth.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        String email,
        String role
) {}