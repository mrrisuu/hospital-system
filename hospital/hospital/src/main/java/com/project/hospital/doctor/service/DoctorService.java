package com.project.hospital.doctor.service;

import com.project.hospital.common.dto.PageResponse;
import com.project.hospital.common.exception.BusinessRuleViolationException;
import com.project.hospital.common.exception.ResourceConflictException;
import com.project.hospital.common.exception.ResourceNotFoundException;
import com.project.hospital.doctor.dto.DoctorCreateRequest;
import com.project.hospital.doctor.dto.DoctorResponse;
import com.project.hospital.doctor.dto.DoctorUpdateRequest;
import com.project.hospital.doctor.entity.Doctor;
import com.project.hospital.doctor.mapper.DoctorMapper;
import com.project.hospital.doctor.repository.DoctorRepository;
import com.project.hospital.doctor.repository.DoctorSpecification;
import com.project.hospital.poli.entity.Poli;
import com.project.hospital.poli.repository.PoliRepository;
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
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final PoliRepository poliRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DoctorMapper doctorMapper;

    @Transactional
    public DoctorResponse create(DoctorCreateRequest request) {
        String email = request.email().trim().toLowerCase();
        String licenseNumber = request.licenseNumber().trim();

        if (userRepository.existsByEmail(email)) {
            throw new ResourceConflictException("Email is already registered");
        }
        if (doctorRepository.existsByLicenseNumber(licenseNumber)) {
            throw new ResourceConflictException("License number already exists");
        }

        Poli poli = findPoli(request.poliId());

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.DOCTOR);
        userRepository.save(user);

        Doctor doctor = doctorMapper.toEntity(request);
        doctor.setLicenseNumber(licenseNumber);
        doctor.setUser(user);
        doctor.setPoli(poli);

        return doctorMapper.toResponse(doctorRepository.save(doctor));
    }

    @Transactional(readOnly = true)
    public PageResponse<DoctorResponse> getAll(Long poliId, String name, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessRuleViolationException(
                    "Page must be at least 0 and size must be between 1 and 100");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));

        Page<Doctor> result = doctorRepository.findAll(
                DoctorSpecification.filter(poliId, name), pageable);
        //jadi diatas method finAll akan mencari data doctor, dan filter disini membuat kondisi where
        //Membuat resep kondisi: WHERE poli_id = ? AND LOWER(full_name) LIKE ? (hanya untuk filter yang diisi)

        return PageResponse.from(result.map(doctorMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public DoctorResponse getById(Long id) {
        return doctorMapper.toResponse(findDoctor(id));
    }

    @Transactional
    public DoctorResponse update(Long id, DoctorUpdateRequest request) {
        Doctor doctor = findDoctor(id);
        String licenseNumber = request.licenseNumber().trim();

        if (doctorRepository.existsByLicenseNumberAndIdNot(licenseNumber, id)) {
            throw new ResourceConflictException("License number already exists");
        }

        Poli poli = findPoli(request.poliId());

        doctorMapper.updateEntity(request, doctor);
        doctor.setLicenseNumber(licenseNumber);
        doctor.setPoli(poli);

        return doctorMapper.toResponse(doctorRepository.save(doctor));
    }

    private Doctor findDoctor(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
    }

    private Poli findPoli(Long id) {
        return poliRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Poli not found"));
    }
}