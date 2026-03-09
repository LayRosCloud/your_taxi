package com.leafall.yourtaxi.dto.point;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PointResponseDto {
    @NotNull
    private Integer index;
    @NotNull
    private String name;
    @NotNull
    private Double longitude;
    @NotNull
    private Double latitude;
}
