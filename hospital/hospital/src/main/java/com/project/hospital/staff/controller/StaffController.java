package com.project.hospital.staff.controller;

import com.project.hospital.common.dto.PageResponse;
import com.project.hospital.staff.dto.StaffCreateRequest;
import com.project.hospital.staff.dto.StaffResponse;
import com.project.hospital.staff.dto.StaffUpdateRequest;
import com.project.hospital.staff.service.StaffService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/staff")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class StaffController {
    private final StaffService staffService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<StaffResponse> create(
            @Valid
            @RequestBody StaffCreateRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(staffService.create(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<PageResponse<StaffResponse>>getAll(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return ResponseEntity.ok(staffService.getAll(name,page,size));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<StaffResponse> getById(
            @PathVariable Long id
            ){
        return ResponseEntity.ok(staffService.getById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<StaffResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody StaffUpdateRequest request
    ) {
        return ResponseEntity.ok(staffService.update(id, request));
    }
}
