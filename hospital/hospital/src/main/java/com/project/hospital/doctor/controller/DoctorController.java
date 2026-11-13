package com.project.hospital.doctor.controller;

import com.project.hospital.common.dto.PageResponse;
import com.project.hospital.doctor.dto.DoctorCreateRequest;
import com.project.hospital.doctor.dto.DoctorResponse;
import com.project.hospital.doctor.dto.DoctorUpdateRequest;
import com.project.hospital.doctor.service.DoctorService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DoctorController {

    private final DoctorService doctorService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<DoctorResponse> create(
            @Valid
            @RequestBody DoctorCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorService.create(request));
    }

    @GetMapping
    public ResponseEntity<PageResponse<DoctorResponse>> getAll(
            @RequestParam(required = false) Long poliId,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(doctorService.getAll(poliId, name, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DoctorUpdateRequest request
    ) {
        return ResponseEntity.ok(doctorService.update(id, request));
    }
}