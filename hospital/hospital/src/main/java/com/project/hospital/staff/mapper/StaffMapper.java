package com.project.hospital.staff.mapper;

import com.project.hospital.staff.dto.StaffCreateRequest;
import com.project.hospital.staff.dto.StaffResponse;
import com.project.hospital.staff.dto.StaffUpdateRequest;
import com.project.hospital.staff.entity.Staff;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface StaffMapper {

    @Mapping(source = "user.email", target = "email")
    StaffResponse toResponse(Staff staff);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Staff toEntity(StaffCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(StaffUpdateRequest request, @MappingTarget Staff staff);
}
