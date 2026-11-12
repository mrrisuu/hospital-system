package com.project.hospital.auth.controller;


import com.project.hospital.auth.dto.LoginRequest;
import com.project.hospital.auth.dto.LoginResponse;
import com.project.hospital.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse>login(
            @Valid @RequestBody LoginRequest loginRequest
            ){
        return ResponseEntity.ok(authService.login(loginRequest));
    }
}
