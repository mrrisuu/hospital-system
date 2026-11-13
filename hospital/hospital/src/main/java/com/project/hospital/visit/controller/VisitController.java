package com.project.hospital.visit.controller;

import com.project.hospital.common.dto.PageResponse;
import com.project.hospital.visit.dto.StaffVisitRegisterRequest;
import com.project.hospital.visit.dto.VisitRegisterRequest;
import com.project.hospital.visit.dto.VisitResponse;
import com.project.hospital.visit.entity.VisitStatus;
import com.project.hospital.visit.service.VisitService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/visits")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class VisitController {

    private final VisitService visitService;

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping("/me")
    public ResponseEntity<VisitResponse> registerMine(
            Authentication authentication,
            @Valid @RequestBody VisitRegisterRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(visitService.registerForSelf(authentication.getName(), request));
    }

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping
    public ResponseEntity<VisitResponse> registerByStaff(
            @Valid @RequestBody StaffVisitRegisterRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(visitService.registerByStaff(request));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/me")
    public ResponseEntity<PageResponse<VisitResponse>> getMine(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(visitService.getMine(authentication.getName(), page, size));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping
    public ResponseEntity<PageResponse<VisitResponse>> getAll(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) VisitStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(visitService.getAll(doctorId, date, status, page, size));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/{id}")
    public ResponseEntity<VisitResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(visitService.getById(id));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PatchMapping("/me/{id}/cancel")
    public ResponseEntity<VisitResponse> cancelMine(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(visitService.cancelOwn(authentication.getName(), id));
    }

    @PreAuthorize("hasRole('STAFF')")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<VisitResponse> cancelByStaff(@PathVariable Long id) {
        return ResponseEntity.ok(visitService.cancelByStaff(id));
    }
}