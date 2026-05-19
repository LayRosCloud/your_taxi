package com.leafall.yourtaxi.dto.order;

import com.leafall.yourtaxi.dto.point.PointResponseDto;
import com.leafall.yourtaxi.dto.trip.TripResponseDto;
import com.leafall.yourtaxi.dto.user.UserResponseDto;
import com.leafall.yourtaxi.entity.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class OrderResponseWithDurationDto {
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
    private List<PointResponseDto> points;
    @Schema(name = "Длительность", example = "23")
    @NotNull(message = "{validation.not-null}")
    public Double durationInSeconds;
}
