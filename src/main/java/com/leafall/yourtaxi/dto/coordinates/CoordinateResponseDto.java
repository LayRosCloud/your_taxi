package com.leafall.yourtaxi.dto.coordinates;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class CoordinateResponseDto {
    @NotNull(message = "{validation.not-null}")
    private UUID id;
    @NotNull(message = "{validation.not-null}")
    private Double longitude;
    @NotNull(message = "{validation.not-null}")
    private Double latitude;
    @NotNull(message = "{validation.not-null}")
    private Double angle;
    private String status;
}
