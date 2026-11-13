package com.project.hospital.poli.controller;

import com.project.hospital.poli.dto.PoliRequest;
import com.project.hospital.poli.dto.PoliResponse;
import com.project.hospital.poli.service.PoliService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/polis")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PoliController {

    private final PoliService poliService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<PoliResponse> create(@Valid @RequestBody PoliRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(poliService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<PoliResponse>> getAll() {
        return ResponseEntity.ok(poliService.getAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<PoliResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PoliRequest request
    ) {
        return ResponseEntity.ok(poliService.update(id, request));
    }
}