package com.leafall.yourtaxi.dto.point;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PointCreateDto {
    @NotNull
    private String name;
    @NotNull
    private Double longitude;
    @NotNull
    private Double latitude;
}
