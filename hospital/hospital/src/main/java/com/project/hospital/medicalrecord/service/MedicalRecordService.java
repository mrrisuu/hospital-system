package com.project.hospital.medicalrecord.service;

import com.project.hospital.common.dto.PageResponse;
import com.project.hospital.common.exception.BusinessRuleViolationException;
import com.project.hospital.common.exception.ResourceConflictException;
import com.project.hospital.common.exception.ResourceNotFoundException;
import com.project.hospital.medicalrecord.dto.MedicalRecordCreateRequest;
import com.project.hospital.medicalrecord.dto.MedicalRecordResponse;
import com.project.hospital.medicalrecord.dto.MedicalRecordUpdateRequest;
import com.project.hospital.medicalrecord.entity.MedicalRecord;
import com.project.hospital.medicalrecord.mapper.MedicalRecordMapper;
import com.project.hospital.medicalrecord.repository.MedicalRecordRepository;
import com.project.hospital.patient.repository.PatientRepository;
import com.project.hospital.visit.entity.Visit;
import com.project.hospital.visit.entity.VisitStatus;
import com.project.hospital.visit.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final VisitRepository visitRepository;
    private final PatientRepository patientRepository;
    private final MedicalRecordMapper medicalRecordMapper;

    @Transactional
    public MedicalRecordResponse create(String doctorEmail, MedicalRecordCreateRequest request) {
        Visit visit = visitRepository.findByIdAndDoctorUserEmail(request.visitId(), doctorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Visit not found"));

        if (medicalRecordRepository.existsByVisitId(visit.getId())) {
            throw new ResourceConflictException("Medical record already exists for this visit");
        }
        if (visit.getStatus() != VisitStatus.CALLED) {
            throw new BusinessRuleViolationException(
                    "Medical record can only be created for a called visit");
        }

        MedicalRecord record = medicalRecordMapper.toEntity(request);
        record.setVisit(visit);
        normalize(record);

        // rekam medis tersimpan dan kunjungan selesai dalam satu transaksi
        visit.setStatus(VisitStatus.COMPLETED);
        visitRepository.save(visit);

        return medicalRecordMapper.toResponse(medicalRecordRepository.save(record));
    }

    @Transactional
    public MedicalRecordResponse update(String doctorEmail, Long id,
                                        MedicalRecordUpdateRequest request) {
        // rekam medis milik dokter lain dianggap "tidak ditemukan"
        MedicalRecord record = medicalRecordRepository
                .findByIdAndVisitDoctorUserEmail(id, doctorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found"));

        medicalRecordMapper.updateEntity(request, record);
        normalize(record);

        return medicalRecordMapper.toResponse(medicalRecordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public PageResponse<MedicalRecordResponse> getMine(String patientEmail, int page, int size) {
        Page<MedicalRecord> result = medicalRecordRepository
                .findByVisitPatientUserEmail(patientEmail, pageable(page, size));

        return PageResponse.from(result.map(medicalRecordMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<MedicalRecordResponse> getByPatient(Long patientId, int page, int size) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found");
        }

        Page<MedicalRecord> result = medicalRecordRepository
                .findByVisitPatientId(patientId, pageable(page, size));

        return PageResponse.from(result.map(medicalRecordMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<MedicalRecordResponse> getByPatientForDoctor(
            String doctorEmail, Long patientId, int page, int size) {
        if (!visitRepository.existsByDoctorUserEmailAndPatientId(doctorEmail, patientId)) {
            throw new AccessDeniedException("You have not handled this patient");
        }

        Page<MedicalRecord> result = medicalRecordRepository
                .findByVisitPatientId(patientId, pageable(page, size));

        return PageResponse.from(result.map(medicalRecordMapper::toResponse));
    }

    /*
        HELPER
     */
    private void normalize(MedicalRecord record) {
        record.setSymptoms(record.getSymptoms().trim());
        record.setDiagnosis(record.getDiagnosis().trim());
        record.setTreatment(
                record.getTreatment() == null || record.getTreatment().isBlank()
                        ? null
                        : record.getTreatment().trim());
    }

    private Pageable pageable(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessRuleViolationException(
                    "Page must be at least 0 and size must be between 1 and 100");
        }
        return PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
    }
}