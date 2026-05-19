package com.leafall.yourtaxi.dto.order;

import com.leafall.yourtaxi.dto.point.PointCreateDto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class OrderCostDto {
    @NotNull(message = "{validation.not-null}")
    private PointCreateDto from;
    @NotNull(message = "{validation.not-null}")
    private PointCreateDto to;
}
