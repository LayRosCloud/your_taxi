package com.leafall.yourtaxi.mapper.impl;

import com.leafall.yourtaxi.dto.employee.EmployeeCreateDto;
import com.leafall.yourtaxi.dto.user.SignUpDto;
import com.leafall.yourtaxi.dto.user.UserInfoResponseDto;
import com.leafall.yourtaxi.dto.user.UserResponseDto;
import com.leafall.yourtaxi.entity.UserEntity;
import com.leafall.yourtaxi.entity.UserInfoEntity;
import com.leafall.yourtaxi.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserMapperImpl implements UserMapper {
    @Value("${backend.url}")
    private String backendUrl;

    @Override
    public UserEntity mapToEntity(SignUpDto dto) {
        if ( dto == null ) {
            return null;
        }

        UserEntity userEntity = new UserEntity();

        userEntity.setFullName( dto.getFullName() );
        userEntity.setEmail( dto.getEmail() );
        userEntity.setPassword( dto.getPassword() );

        return userEntity;
    }

    @Override
    public UserEntity mapToEntity(EmployeeCreateDto dto) {
        if ( dto == null ) {
            return null;
        }

        UserEntity userEntity = new UserEntity();

        userEntity.setFullName( dto.getFullName() );
        userEntity.setEmail( dto.getEmail() );

        return userEntity;
    }

    @Override
    public UserResponseDto mapToDto(UserEntity entity) {
        if ( entity == null ) {
            return null;
        }

        UserResponseDto userResponseDto = new UserResponseDto();

        userResponseDto.setId( entity.getId() );
        userResponseDto.setEmail( entity.getEmail() );
        userResponseDto.setFullName( entity.getFullName() );
        if ( entity.getRole() != null ) {
            userResponseDto.setRole( entity.getRole().name() );
        }
        userResponseDto.setIsActive( entity.getIsActive() );
        userResponseDto.setCreatedAt( entity.getCreatedAt() );
        if (entity.getAvatar() != null) {
            userResponseDto.setAvatar(String.format("%s/v1/files/avatars/%s", backendUrl, entity.getAvatar()));
        }
        userResponseDto.setInfo( userInfoEntityToUserInfoResponseDto( entity.getInfo() ) );

        return userResponseDto;
    }

    protected UserInfoResponseDto userInfoEntityToUserInfoResponseDto(UserInfoEntity userInfoEntity) {
        if ( userInfoEntity == null ) {
            return null;
        }

        UserInfoResponseDto userInfoResponseDto = new UserInfoResponseDto();

        userInfoResponseDto.setPhone( userInfoEntity.getPhone() );

        return userInfoResponseDto;
    }
}
