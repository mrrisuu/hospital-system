package com.project.hospital.patient.service;

import com.project.hospital.common.dto.PageResponse;
import com.project.hospital.common.exception.BusinessRuleViolationException;
import com.project.hospital.common.exception.ResourceConflictException;
import com.project.hospital.common.exception.ResourceNotFoundException;
import com.project.hospital.patient.dto.PatientRegisterRequest;
import com.project.hospital.patient.dto.PatientResponse;
import com.project.hospital.patient.entity.Patient;
import com.project.hospital.patient.mapper.PatientMapper;
import com.project.hospital.patient.repository.PatientRepository;
import com.project.hospital.user.entity.Role;
import com.project.hospital.user.entity.User;
import com.project.hospital.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PatientMapper patientMapper;

    @Transactional
    public PatientResponse register(PatientRegisterRequest request) {
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ResourceConflictException("Email is already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.PATIENT);
        userRepository.save(user);

        Patient patient = patientMapper.toEntity(request);
        patient.setUser(user);
        patient.setMedicalRecordNumber(generateMedicalRecordNumber());

        return patientMapper.toResponse(patientRepository.save(patient));
    }

    @Transactional(readOnly = true)
    public PatientResponse getByEmail(String email) {
        Patient patient = patientRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));
        return patientMapper.toResponse(patient);
    }

    @Transactional(readOnly = true)
    public PatientResponse getById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        return patientMapper.toResponse(patient);
    }

    @Transactional(readOnly = true)
    public PageResponse<PatientResponse> getAll(String name, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessRuleViolationException(
                    "Page must be at least 0 and size must be between 1 and 100");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));

        Page<Patient> result = (name == null || name.isBlank())
                ? patientRepository.findAll(pageable)
                : patientRepository.findByFullNameContainingIgnoreCase(name.trim(), pageable);

        return PageResponse.from(result.map(patientMapper::toResponse));
    }

    // Format: RM + 6 digit, contoh RM000001
    private String generateMedicalRecordNumber() {
        return String.format("RM%06d", patientRepository.nextMedicalRecordSequence());
    }
}