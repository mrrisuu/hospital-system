package com.project.hospital.visit.service;

import com.project.hospital.common.dto.PageResponse;
import com.project.hospital.common.exception.BusinessRuleViolationException;
import com.project.hospital.common.exception.ResourceConflictException;
import com.project.hospital.common.exception.ResourceNotFoundException;
import com.project.hospital.doctor.entity.Doctor;
import com.project.hospital.doctor.repository.DoctorRepository;
import com.project.hospital.patient.entity.Patient;
import com.project.hospital.patient.repository.PatientRepository;
import com.project.hospital.schedule.entity.DoctorSchedule;
import com.project.hospital.schedule.repository.DoctorScheduleRepository;
import com.project.hospital.visit.dto.StaffVisitRegisterRequest;
import com.project.hospital.visit.dto.VisitRegisterRequest;
import com.project.hospital.visit.dto.VisitResponse;
import com.project.hospital.visit.entity.Visit;
import com.project.hospital.visit.entity.VisitStatus;
import com.project.hospital.visit.mapper.VisitMapper;
import com.project.hospital.visit.repository.VisitRepository;
import com.project.hospital.visit.repository.VisitSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class VisitService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Jakarta");
    private static final int MAX_DAYS_AHEAD = 30;

    private final VisitRepository visitRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final VisitMapper visitMapper;

    @Transactional
    public VisitResponse registerForSelf(String email, VisitRegisterRequest request) {
        Patient patient = patientRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        return register(patient, request.scheduleId(), request.visitDate(), request.complaint());
    }

    @Transactional
    public VisitResponse registerByStaff(StaffVisitRegisterRequest request) {
        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        return register(patient, request.scheduleId(), request.visitDate(), request.complaint());
    }

    @Transactional(readOnly = true)
    public PageResponse<VisitResponse> getMine(String email, int page, int size) {
        Pageable pageable = pageable(page, size,
                Sort.by(Sort.Order.desc("visitDate"), Sort.Order.desc("id")));

        Page<Visit> result = visitRepository.findByPatientUserEmail(email, pageable);

        return PageResponse.from(result.map(visitMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<VisitResponse> getAll(Long doctorId, LocalDate visitDate,
                                              VisitStatus status, int page, int size) {
        Pageable pageable = pageable(page, size,
                Sort.by(Sort.Order.desc("visitDate"), Sort.Order.asc("queueNumber")));

        Page<Visit> result = visitRepository.findAll(
                VisitSpecification.filter(doctorId, visitDate, status), pageable);

        return PageResponse.from(result.map(visitMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public VisitResponse getById(Long id) {
        return visitMapper.toResponse(findVisit(id));
    }

    @Transactional
    public VisitResponse cancelOwn(String email, Long id) {
        // kunjungan milik pasien lain dianggap "tidak ditemukan" (tidak membocorkan keberadaannya)
        Visit visit = visitRepository.findByIdAndPatientUserEmail(id, email)
                .orElseThrow(() -> new ResourceNotFoundException("Visit not found"));

        return cancel(visit);
    }

    @Transactional
    public VisitResponse cancelByStaff(Long id) {
        return cancel(findVisit(id));
    }

    /*
        HELPER
     */
    private VisitResponse register(Patient patient, Long scheduleId,
                                   LocalDate visitDate, String complaint) {
        DoctorSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));

        validateDate(schedule, visitDate);

        // kunci baris dokter: pendaftaran untuk dokter yang sama berjalan satu per satu
        Long doctorId = schedule.getDoctor().getId();
        Doctor doctor = doctorRepository.findByIdForUpdate(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        if (visitRepository.existsByPatientIdAndDoctorIdAndVisitDateAndStatusNot(
                patient.getId(), doctorId, visitDate, VisitStatus.CANCELLED)) {
            throw new ResourceConflictException(
                    "Patient is already registered with this doctor on that date");
        }

        long taken = visitRepository.countByScheduleIdAndVisitDateAndStatusNot(
                scheduleId, visitDate, VisitStatus.CANCELLED);
        if (taken >= schedule.getQuota()) {
            throw new ResourceConflictException("Quota for this schedule is full");
        }

        int queueNumber = visitRepository.findMaxQueueNumber(doctorId, visitDate) + 1;

        Visit visit = new Visit();
        visit.setPatient(patient);
        visit.setDoctor(doctor);
        visit.setSchedule(schedule);
        visit.setVisitDate(visitDate);
        visit.setQueueNumber(queueNumber);
        visit.setStatus(VisitStatus.WAITING);
        visit.setComplaint(complaint == null || complaint.isBlank() ? null : complaint.trim());

        return visitMapper.toResponse(visitRepository.save(visit));
    }

    private void validateDate(DoctorSchedule schedule, LocalDate visitDate) {
        LocalDate today = LocalDate.now(ZONE);

        if (visitDate.isBefore(today)) {
            throw new BusinessRuleViolationException("Visit date must not be in the past");
        }
        if (visitDate.isAfter(today.plusDays(MAX_DAYS_AHEAD))) {
            throw new BusinessRuleViolationException(
                    "Visit date must be within " + MAX_DAYS_AHEAD + " days from today");
        }
        if (visitDate.getDayOfWeek() != schedule.getDayOfWeek()) {
            throw new BusinessRuleViolationException(
                    "Visit date does not match the schedule day (" + schedule.getDayOfWeek() + ")");
        }
        if (visitDate.equals(today) && !LocalTime.now(ZONE).isBefore(schedule.getEndTime())) {
            throw new BusinessRuleViolationException("Schedule has already ended for today");
        }
    }

    private VisitResponse cancel(Visit visit) {
        if (visit.getStatus() != VisitStatus.WAITING) {
            throw new BusinessRuleViolationException("Only waiting visits can be cancelled");
        }

        visit.setStatus(VisitStatus.CANCELLED);

        return visitMapper.toResponse(visitRepository.save(visit));
    }

    private Visit findVisit(Long id) {
        return visitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visit not found"));
    }

    private Pageable pageable(int page, int size, Sort sort) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessRuleViolationException(
                    "Page must be at least 0 and size must be between 1 and 100");
        }
        return PageRequest.of(page, size, sort);
    }
}