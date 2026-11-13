package com.project.hospital.medicalrecord.mapper;

import com.project.hospital.medicalrecord.dto.MedicalRecordCreateRequest;
import com.project.hospital.medicalrecord.dto.MedicalRecordResponse;
import com.project.hospital.medicalrecord.dto.MedicalRecordUpdateRequest;
import com.project.hospital.medicalrecord.entity.MedicalRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MedicalRecordMapper {

    @Mapping(source = "visit.id", target = "visitId")
    @Mapping(source = "visit.visitDate", target = "visitDate")
    @Mapping(source = "visit.patient.id", target = "patientId")
    @Mapping(source = "visit.patient.fullName", target = "patientName")
    @Mapping(source = "visit.patient.medicalRecordNumber", target = "medicalRecordNumber")
    @Mapping(source = "visit.doctor.id", target = "doctorId")
    @Mapping(source = "visit.doctor.fullName", target = "doctorName")
    @Mapping(source = "visit.doctor.poli.name", target = "poliName")
    MedicalRecordResponse toResponse(MedicalRecord record);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "visit", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    MedicalRecord toEntity(MedicalRecordCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "visit", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(MedicalRecordUpdateRequest request, @MappingTarget MedicalRecord record);
}