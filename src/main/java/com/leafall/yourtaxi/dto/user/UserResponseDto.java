package com.leafall.yourtaxi.dto.user;

import lombok.Data;

import java.util.UUID;

@Data
public class UserResponseDto {
    private UUID id;
    private String email;
    private String fullName;
    private String role;
    private Boolean isActive;
    private Long createdAt;
    private UserInfoResponseDto info;
}
