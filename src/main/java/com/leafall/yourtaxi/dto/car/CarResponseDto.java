package com.leafall.yourtaxi.dto.car;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CarResponseDto {
    @Schema(description = "Id машины", example = "1")
    @NotNull
    private Long id;

    @Schema(description = "Номер машины", example = "T222ZZ11 22")
    @NotNull
    private String number;

    @Schema(description = "Марка машины", example = "Honda")
    @NotNull
    private String mark;

    @Schema(description = "Цвет машины", example = "Белый")
    @NotNull
    private String color;

    @Schema(description = "Дата создания машины", example = "1766091600000")
    @NotNull
    private Long createdAt;
}
