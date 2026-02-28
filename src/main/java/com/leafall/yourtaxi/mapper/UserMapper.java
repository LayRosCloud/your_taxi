package com.leafall.yourtaxi.mapper;

import com.leafall.yourtaxi.dto.employee.EmployeeCreateDto;
import com.leafall.yourtaxi.dto.user.SignUpDto;
import com.leafall.yourtaxi.dto.user.UserResponseDto;
import com.leafall.yourtaxi.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserEntity mapToEntity(SignUpDto dto);
    UserEntity mapToEntity(EmployeeCreateDto dto);
    UserResponseDto mapToDto(UserEntity entity);
}
