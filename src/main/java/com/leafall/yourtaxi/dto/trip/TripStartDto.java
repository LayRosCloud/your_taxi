package com.leafall.yourtaxi.dto.trip;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TripStartDto {
    @Schema(description = "id машины", example = "1")
    @Min(value = 0, message = "Id не может быть меньше 0")
    @NotNull
    private Long carId;
}
