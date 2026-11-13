package com.project.hospital.medicalrecord.controller;

import com.project.hospital.common.dto.PageResponse;
import com.project.hospital.medicalrecord.dto.MedicalRecordCreateRequest;
import com.project.hospital.medicalrecord.dto.MedicalRecordResponse;
import com.project.hospital.medicalrecord.dto.MedicalRecordUpdateRequest;
import com.project.hospital.medicalrecord.service.MedicalRecordService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/medical-records")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping
    public ResponseEntity<MedicalRecordResponse> create(
            Authentication authentication,
            @Valid @RequestBody MedicalRecordCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(medicalRecordService.create(authentication.getName(), request));
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PutMapping("/{id}")
    public ResponseEntity<MedicalRecordResponse> update(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody MedicalRecordUpdateRequest request
    ) {
        return ResponseEntity.ok(
                medicalRecordService.update(authentication.getName(), id, request));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/me")
    public ResponseEntity<PageResponse<MedicalRecordResponse>> getMine(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                medicalRecordService.getMine(authentication.getName(), page, size));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<PageResponse<MedicalRecordResponse>> getByPatient(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(medicalRecordService.getByPatient(patientId, page, size));
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/my-patients/{patientId}")
    public ResponseEntity<PageResponse<MedicalRecordResponse>> getByPatientForDoctor(
            Authentication authentication,
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(medicalRecordService.getByPatientForDoctor(
                authentication.getName(), patientId, page, size));
    }
}