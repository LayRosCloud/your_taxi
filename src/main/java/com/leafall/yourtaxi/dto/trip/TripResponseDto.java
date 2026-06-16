package com.leafall.yourtaxi.dto.trip;

import com.leafall.yourtaxi.dto.car.CarResponseDto;
import com.leafall.yourtaxi.dto.user.UserResponseDto;
import com.leafall.yourtaxi.entity.enums.TripStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class TripResponseDto {
    @Schema(description = "Id", example = "9f76f9e4-c370-4066-bc21-1fffe0d4adcf")
    @NotNull(message = "{validation.not-null}")
    private UUID id;

    @Schema(description = "Пользователь")
    @NotNull(message = "{validation.not-null}")
    private UserResponseDto user;

    @Schema(description = "Машина")
    @NotNull(message = "{validation.not-null}")
    private CarResponseDto car;

    @Schema(description = "Дата создания", example = "1766091600000")
    @NotNull(message = "{validation.not-null}")
    private Long createdAt;

    @Schema(description = "Статус водителя")
    private TripStatus status;

    @Schema(description = "Дата завершения", example = "1766091600000")
    private Long endAt;
}
