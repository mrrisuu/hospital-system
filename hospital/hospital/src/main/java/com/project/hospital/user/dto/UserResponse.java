package com.project.hospital.user.dto;

import com.project.hospital.user.entity.Role;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        Role role,
        boolean active,
        LocalDateTime createdAt
) {}