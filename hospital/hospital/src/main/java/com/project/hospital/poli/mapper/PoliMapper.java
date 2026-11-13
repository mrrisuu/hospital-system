package com.project.hospital.poli.mapper;

import com.project.hospital.poli.dto.PoliRequest;
import com.project.hospital.poli.dto.PoliResponse;
import com.project.hospital.poli.entity.Poli;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PoliMapper {

    PoliResponse toResponse(Poli poli);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Poli toEntity(PoliRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(PoliRequest request, @MappingTarget Poli poli);
}