package com.leafall.yourtaxi.dto.order;

import com.leafall.yourtaxi.dto.car.CarResponseDto;
import com.leafall.yourtaxi.dto.point.PointResponseDto;
import com.leafall.yourtaxi.dto.user.UserResponseDto;
import com.leafall.yourtaxi.entity.enums.OrderStatus;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class OrderResponseDto {
    private UUID id;
    private UserResponseDto user;
    private CarResponseDto executor;
    private OrderStatus status;
    private Long createdAt;
    private List<PointResponseDto> points;
}
