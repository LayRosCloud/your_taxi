package com.leafall.yourtaxi.dto.coordinates;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class CoordinateResponseDto {
    @NotNull
    private UUID id;
    @NotNull
    private Double longitude;
    @NotNull
    private Double latitude;
    @NotNull
    private Double angle;
}
