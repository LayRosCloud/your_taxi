package com.leafall.yourtaxi.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class OrderSetDriverAndDate {
    @NotNull(message = "{validation.not-null}")
    private UUID id;
    @NotNull(message = "{validation.not-null}")
    private Date date;
    @NotNull(message = "{validation.not-null}")
    private UUID driverId;
}
