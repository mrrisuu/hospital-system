package com.project.hospital.visit.mapper;

import com.project.hospital.visit.dto.VisitResponse;
import com.project.hospital.visit.entity.Visit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VisitMapper {

    @Mapping(source = "patient.id", target = "patientId")
    @Mapping(source = "patient.fullName", target = "patientName")
    @Mapping(source = "patient.medicalRecordNumber", target = "medicalRecordNumber")
    @Mapping(source = "doctor.id", target = "doctorId")
    @Mapping(source = "doctor.fullName", target = "doctorName")
    @Mapping(source = "doctor.poli.name", target = "poliName")
    @Mapping(source = "schedule.id", target = "scheduleId")
    @Mapping(source = "schedule.startTime", target = "startTime")
    @Mapping(source = "schedule.endTime", target = "endTime")
    VisitResponse toResponse(Visit visit);
}