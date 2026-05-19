package com.leafall.yourtaxi.dto.point;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PointCreateDto {
    @NotNull(message = "{validation.not-null}")
    private String name;
    @NotNull(message = "{validation.not-null}")
    private Double longitude;
    @NotNull(message = "{validation.not-null}")
    private Double latitude;
}
