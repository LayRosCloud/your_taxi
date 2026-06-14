package com.leafall.yourtaxi.dto.generatedFiles;

import com.leafall.yourtaxi.dto.user.UserResponseDto;
import com.leafall.yourtaxi.entity.enums.GeneratedFileStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class GeneratedFileResponseDto {
    private UUID id;
    private String path;
    private GeneratedFileStatus status;
    private UserResponseDto user;
    private Long createdAt;
}
