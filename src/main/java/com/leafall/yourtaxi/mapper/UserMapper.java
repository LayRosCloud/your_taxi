package com.leafall.yourtaxi.mapper;

import com.leafall.yourtaxi.dto.employee.EmployeeCreateDto;
import com.leafall.yourtaxi.dto.user.SignUpDto;
import com.leafall.yourtaxi.dto.user.UserDetailResponseDto;
import com.leafall.yourtaxi.dto.user.UserResponseDto;
import com.leafall.yourtaxi.entity.UserEntity;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Value;

public interface UserMapper {
    UserEntity mapToEntity(SignUpDto dto);
    UserEntity mapToEntity(EmployeeCreateDto dto);
    UserResponseDto mapToDto(UserEntity entity);
    UserDetailResponseDto mapToDetailDto(UserEntity entity);
}
