package com.leafall.yourtaxi.dto.feedback;

import com.leafall.yourtaxi.dto.user.UserResponseDto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class FeedbackResponseDto {
    @NotNull
    private UUID id;
    @NotNull
    private String title;
    @NotNull
    private String description;
    private String phone;
    private UserResponseDto createdBy;
    private Long readAt;
    private Long createdAt;
}
