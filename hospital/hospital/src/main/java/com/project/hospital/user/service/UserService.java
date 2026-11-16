package com.project.hospital.user.service;

import com.project.hospital.common.dto.PageResponse;
import com.project.hospital.common.exception.BusinessRuleViolationException;
import com.project.hospital.common.exception.ResourceNotFoundException;
import com.project.hospital.user.dto.ChangePasswordRequest;
import com.project.hospital.user.dto.UserResponse;
import com.project.hospital.user.entity.Role;
import com.project.hospital.user.entity.User;
import com.project.hospital.user.mapper.UserMapper;
import com.project.hospital.user.repository.UserRepository;
import com.project.hospital.user.repository.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessRuleViolationException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BusinessRuleViolationException(
                    "New password must be different from the current password");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        // semua token yang diterbitkan sebelum waktu ini tidak berlaku lagi
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAll(Role role, Boolean active, String email,
                                             int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessRuleViolationException(
                    "Page must be at least 0 and size must be between 1 and 100");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));

        Page<User> result = userRepository.findAll(
                UserSpecification.filter(role, active, email), pageable);

        return PageResponse.from(result.map(userMapper::toResponse));
    }

    @Transactional
    public UserResponse updateStatus(String adminEmail, Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!active && user.getEmail().equals(adminEmail)) {
            throw new BusinessRuleViolationException("You cannot deactivate your own account");
        }

        user.setActive(active);

        return userMapper.toResponse(userRepository.save(user));
    }
}