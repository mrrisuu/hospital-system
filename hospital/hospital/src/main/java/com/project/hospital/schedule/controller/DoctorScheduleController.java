package com.project.hospital.schedule.controller;

import com.project.hospital.schedule.dto.ScheduleRequest;
import com.project.hospital.schedule.dto.ScheduleResponse;
import com.project.hospital.schedule.service.DoctorScheduleService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DoctorScheduleController {

    private final DoctorScheduleService scheduleService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/doctors/{doctorId}/schedules")
    public ResponseEntity<ScheduleResponse> create(
            @PathVariable Long doctorId,
            @Valid @RequestBody ScheduleRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(scheduleService.create(doctorId, request));
    }

    @GetMapping("/doctors/{doctorId}/schedules")
    public ResponseEntity<List<ScheduleResponse>> getByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(scheduleService.getByDoctor(doctorId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/schedules/{id}")
    public ResponseEntity<ScheduleResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ScheduleRequest request
    ) {
        return ResponseEntity.ok(scheduleService.update(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/schedules/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        scheduleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}