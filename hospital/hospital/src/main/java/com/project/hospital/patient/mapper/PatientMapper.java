package com.project.hospital.patient.mapper;

import com.project.hospital.patient.dto.PatientRegisterRequest;
import com.project.hospital.patient.dto.PatientResponse;
import com.project.hospital.patient.entity.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PatientMapper {

    @Mapping(source = "user.email", target = "email")
    PatientResponse toResponse(Patient patient);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "medicalRecordNumber", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Patient toEntity(PatientRegisterRequest request);
}