package com.leafall.yourtaxi.dto.order;

import com.leafall.yourtaxi.dto.car.CarResponseDto;
import com.leafall.yourtaxi.dto.point.PointResponseDto;
import com.leafall.yourtaxi.dto.trip.TripResponseDto;
import com.leafall.yourtaxi.dto.user.UserResponseDto;
import com.leafall.yourtaxi.entity.enums.OrderPaymentType;
import com.leafall.yourtaxi.entity.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class OrderResponseDto {
    @NotNull(message = "{validation.not-null}")
    private UUID id;
    @NotNull(message = "{validation.not-null}")
    private UserResponseDto user;
    private TripResponseDto executor;
    @NotNull(message = "{validation.not-null}")
    private OrderStatus status;
    @NotNull(message = "{validation.not-null}")
    private Double price;
    @NotNull(message = "{validation.not-null}")
    private Long createdAt;
    @NotNull(message = "{validation.not-null}")
    private Boolean isBigDistance;
    @NotNull(message = "{validation.not-null}")
    private OrderPaymentType paymentType;
    private UserResponseDto plannerDriver;
    private Long scheduledStartTime;
    @NotNull(message = "{validation.not-null}")
    private List<PointResponseDto> points;
}
