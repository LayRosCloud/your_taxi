package com.leafall.yourtaxi.dto.order;

import com.leafall.yourtaxi.entity.enums.OrderPaymentType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class OrderCreateDto extends OrderCostDto {
    @NotNull(message = "{validation.not-null}")
    private OrderPaymentType paymentType;
}
