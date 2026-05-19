package com.leafall.yourtaxi.dto.point;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
public class PointCostDto {
    @NotNull(message = "{validation.not-null}")
    private Double price;
    @NotNull(message = "{validation.not-null}")
    private Double durationInSeconds;
    @NotNull(message = "{validation.not-null}")
    private Boolean isBigDistance;
}
