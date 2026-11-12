package com.project.hospital.auth.service;

import com.project.hospital.auth.dto.LoginRequest;
import com.project.hospital.auth.dto.LoginResponse;
import com.project.hospital.security.JwtService;
import com.project.hospital.user.entity.User;
import com.project.hospital.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        // Melempar BadCredentialsException / DisabledException bila gagal
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password())
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        String role = user.getRole().name();
        String token = jwtService.generateToken(user.getEmail(), role);

        return new LoginResponse(token, "Bearer", user.getEmail(), role);
    }
}