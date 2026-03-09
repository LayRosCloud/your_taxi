package com.leafall.yourtaxi.dto.point;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
public class PointCostDto {
    @NotNull
    private Double price;
    @NotNull
    private Double durationInSeconds;
}
