package com.project.hospital.schedule.service;

import com.project.hospital.common.exception.BusinessRuleViolationException;
import com.project.hospital.common.exception.ResourceConflictException;
import com.project.hospital.common.exception.ResourceNotFoundException;
import com.project.hospital.doctor.entity.Doctor;
import com.project.hospital.doctor.repository.DoctorRepository;
import com.project.hospital.schedule.dto.ScheduleRequest;
import com.project.hospital.schedule.dto.ScheduleResponse;
import com.project.hospital.schedule.entity.DoctorSchedule;
import com.project.hospital.schedule.mapper.DoctorScheduleMapper;
import com.project.hospital.schedule.repository.DoctorScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorScheduleService {

    private final DoctorScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorScheduleMapper scheduleMapper;

    @Transactional
    public ScheduleResponse create(Long doctorId, ScheduleRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        validateTime(request);

        if (scheduleRepository.existsOverlap(
                doctorId, request.dayOfWeek(), request.startTime(), request.endTime())) {
            throw new ResourceConflictException("Schedule overlaps with an existing schedule");
        }

        DoctorSchedule schedule = scheduleMapper.toEntity(request);
        schedule.setDoctor(doctor);

        return scheduleMapper.toResponse(scheduleRepository.save(schedule));
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getByDoctor(Long doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found");
        }

        return scheduleRepository.findByDoctorId(doctorId).stream()
                .sorted(Comparator
                        .comparing(DoctorSchedule::getDayOfWeek)
                        .thenComparing(DoctorSchedule::getStartTime))
                .map(scheduleMapper::toResponse)
                .toList();
    }

    @Transactional
    public ScheduleResponse update(Long id, ScheduleRequest request) {
        DoctorSchedule schedule = findSchedule(id);

        validateTime(request);

        Long doctorId = schedule.getDoctor().getId();
        if (scheduleRepository.existsOverlapExcluding(
                doctorId, request.dayOfWeek(), request.startTime(), request.endTime(), id)) {
            throw new ResourceConflictException("Schedule overlaps with an existing schedule");
        }

        scheduleMapper.updateEntity(request, schedule);

        return scheduleMapper.toResponse(scheduleRepository.save(schedule));
    }

    @Transactional
    public void delete(Long id) {
        scheduleRepository.delete(findSchedule(id));
    }

    private DoctorSchedule findSchedule(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));
    }

    private void validateTime(ScheduleRequest request) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BusinessRuleViolationException("End time must be after start time");
        }
    }
}