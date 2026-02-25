package com.leafall.yourtaxi.dto.car;

import com.leafall.yourtaxi.dto.user.UserResponseDto;
import lombok.Data;

@Data
public class CarResponseDto {
    private Long id;
    private UserResponseDto worker;
    private String number;
    private String mark;
    private String color;
    private Long createdAt;
}
