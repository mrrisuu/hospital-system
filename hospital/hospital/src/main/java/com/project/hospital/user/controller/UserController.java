package com.project.hospital.user.controller;

import com.project.hospital.user.dto.MeResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(Authentication authentication) {
        String role = authentication.getAuthorities().iterator().next()
                .getAuthority().replace("ROLE_", "");
        return ResponseEntity.ok(new MeResponse(authentication.getName(), role));
    }
}