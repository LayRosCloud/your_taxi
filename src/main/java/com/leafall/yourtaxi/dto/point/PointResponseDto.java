package com.leafall.yourtaxi.dto.point;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PointResponseDto {
    @NotNull(message = "{validation.not-null}")
    private Integer index;
    @NotNull(message = "{validation.not-null}")
    private String name;
    @NotNull(message = "{validation.not-null}")
    private Double longitude;
    @NotNull(message = "{validation.not-null}")
    private Double latitude;
}
