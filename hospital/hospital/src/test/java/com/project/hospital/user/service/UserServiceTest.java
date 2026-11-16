package com.project.hospital.user.service;

import com.project.hospital.common.exception.BusinessRuleViolationException;
import com.project.hospital.common.exception.ResourceNotFoundException;
import com.project.hospital.user.dto.ChangePasswordRequest;
import com.project.hospital.user.entity.User;
import com.project.hospital.user.mapper.UserMapper;
import com.project.hospital.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String EMAIL = "budi@hospital.local";
    private static final String ADMIN_EMAIL = "admin@hospital.local";

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User userWithPassword(String email, String hashedPassword) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(hashedPassword);
        return user;
    }

    // ---------- ganti password ----------

    @Test
    void changePassword_success_updatesHashAndTimestamp() {
        User user = userWithPassword(EMAIL, "hashedOld");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass123", "hashedOld")).thenReturn(true);
        when(passwordEncoder.matches("newPass123", "hashedOld")).thenReturn(false);
        when(passwordEncoder.encode("newPass123")).thenReturn("hashedNew");

        userService.changePassword(EMAIL, new ChangePasswordRequest("oldPass123", "newPass123"));

        assertThat(user.getPassword()).isEqualTo("hashedNew");
        assertThat(user.getPasswordChangedAt()).isNotNull();
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_wrongCurrentPassword_throwsBusinessRule() {
        User user = userWithPassword(EMAIL, "hashedOld");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPass1", "hashedOld")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(
                EMAIL, new ChangePasswordRequest("wrongPass1", "newPass123")))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Current password is incorrect");

        assertThat(user.getPassword()).isEqualTo("hashedOld");
        assertThat(user.getPasswordChangedAt()).isNull();
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_newPasswordSameAsCurrent_throwsBusinessRule() {
        User user = userWithPassword(EMAIL, "hashedOld");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("samePass123", "hashedOld")).thenReturn(true);

        assertThatThrownBy(() -> userService.changePassword(
                EMAIL, new ChangePasswordRequest("samePass123", "samePass123")))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("must be different");

        verify(userRepository, never()).save(any());
    }

    // ---------- aktivasi akun ----------

    @Test
    void updateStatus_deactivateAnotherUser_setsInactive() {
        User target = userWithPassword("dokter@hospital.local", "hashed");
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        userService.updateStatus(ADMIN_EMAIL, 2L, false);

        assertThat(target.isActive()).isFalse();
        verify(userRepository).save(target);
    }

    @Test
    void updateStatus_deactivateOwnAccount_throwsBusinessRule() {
        User admin = userWithPassword(ADMIN_EMAIL, "hashed");
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> userService.updateStatus(ADMIN_EMAIL, 1L, false))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("You cannot deactivate your own account");

        assertThat(admin.isActive()).isTrue();
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateStatus_userNotFound_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateStatus(ADMIN_EMAIL, 99L, false))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}