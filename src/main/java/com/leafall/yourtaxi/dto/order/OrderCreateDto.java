package com.leafall.yourtaxi.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class OrderCreateDto extends OrderCostDto {
    @NotNull(message = "{validation.not-null}")
    @Min(value = 0, message = "{validation.radius.min}")
    private Double radius;
}
