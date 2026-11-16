package com.project.hospital.user.mapper;

import com.project.hospital.user.dto.UserResponse;
import com.project.hospital.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}