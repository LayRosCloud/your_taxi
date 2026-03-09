package com.leafall.yourtaxi.dto.order;

import com.leafall.yourtaxi.dto.car.CarResponseDto;
import com.leafall.yourtaxi.dto.point.PointResponseDto;
import com.leafall.yourtaxi.dto.trip.TripResponseDto;
import com.leafall.yourtaxi.dto.user.UserResponseDto;
import com.leafall.yourtaxi.entity.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class OrderResponseDto {
    @NotNull
    private UUID id;
    @NotNull
    private UserResponseDto user;
    private TripResponseDto executor;
    @NotNull
    private OrderStatus status;
    @NotNull
    private Double price;
    @NotNull
    private Long createdAt;
    @NotNull
    private List<PointResponseDto> points;
}
