package com.project.hospital.schedule.mapper;

import com.project.hospital.schedule.dto.ScheduleRequest;
import com.project.hospital.schedule.dto.ScheduleResponse;
import com.project.hospital.schedule.entity.DoctorSchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DoctorScheduleMapper {

    @Mapping(source = "doctor.id", target = "doctorId")
    ScheduleResponse toResponse(DoctorSchedule schedule);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    DoctorSchedule toEntity(ScheduleRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(ScheduleRequest request, @MappingTarget DoctorSchedule schedule);
}