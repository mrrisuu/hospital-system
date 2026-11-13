package com.project.hospital.doctor.mapper;

import com.project.hospital.doctor.dto.DoctorCreateRequest;
import com.project.hospital.doctor.dto.DoctorResponse;
import com.project.hospital.doctor.dto.DoctorUpdateRequest;
import com.project.hospital.doctor.entity.Doctor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DoctorMapper {

    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "poli.id", target = "poliId")
    @Mapping(source = "poli.name", target = "poliName")
    DoctorResponse toResponse(Doctor doctor);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "poli", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Doctor toEntity(DoctorCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "poli", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(DoctorUpdateRequest request, @MappingTarget Doctor doctor);
}