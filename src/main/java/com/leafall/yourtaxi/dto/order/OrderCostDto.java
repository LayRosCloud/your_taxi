package com.leafall.yourtaxi.dto.order;

import com.leafall.yourtaxi.dto.point.PointCreateDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderCostDto {
    @NotNull
    private PointCreateDto from;
    @NotNull
    private PointCreateDto to;
}
