package com.project.hospital.staff.service;

import com.project.hospital.common.dto.PageResponse;
import com.project.hospital.common.exception.BusinessRuleViolationException;
import com.project.hospital.common.exception.ResourceConflictException;
import com.project.hospital.common.exception.ResourceNotFoundException;
import com.project.hospital.staff.dto.StaffCreateRequest;
import com.project.hospital.staff.dto.StaffResponse;
import com.project.hospital.staff.dto.StaffUpdateRequest;
import com.project.hospital.staff.entity.Staff;
import com.project.hospital.staff.mapper.StaffMapper;
import com.project.hospital.staff.repository.StaffRepository;
import com.project.hospital.user.entity.Role;
import com.project.hospital.user.entity.User;
import com.project.hospital.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StaffService {
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StaffMapper staffMapper;

    @Transactional
    public StaffResponse create(StaffCreateRequest request){
        String email = request.email().trim().toLowerCase();

        if(userRepository.existsByEmail(email)){
            throw new ResourceConflictException("Email is already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.STAFF);
        userRepository.save(user);

        Staff staff = staffMapper.toEntity(request);
        staff.setUser(user);

        return staffMapper.toResponse(staffRepository.save(staff));
    }

    @Transactional(readOnly = true)
    public PageResponse<StaffResponse> getAll(
            String name, int page, int size){
        if (page<0||size<1||size>100){
            throw new BusinessRuleViolationException(
                    "Page must be at least 0 and size must be between 1 and 100"
            );
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));

        Page<Staff> result = (name == null || name.isBlank())
                ? staffRepository.findAll(pageable)
                : staffRepository.findByFullNameContainingIgnoreCase(name.trim(), pageable);

        return PageResponse.from(result.map(staffMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public StaffResponse getById(Long id){
        return staffMapper.toResponse(findStaff(id));
    }

    @Transactional
    public StaffResponse update(Long id, StaffUpdateRequest request){
        Staff staff = findStaff(id);

        staffMapper.updateEntity(request,staff);

        return staffMapper.toResponse(staffRepository.save(staff));
    }


    /*
        HELPER
     */
    private Staff findStaff(Long id){
        return staffRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Staff not found"));
    }
}
