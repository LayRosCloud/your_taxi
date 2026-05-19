package com.leafall.yourtaxi.dto.car;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class CarUpdateDto {
    @Schema(description = "Id машины", example = "1")
    @NotNull(message = "{validation.not-null}")
    @Min(value = 0, message = "{validation.id.min}")
    private Long id;

    @Schema(description = "Номер машины", example = "T222ZZ11 22")
    @NotNull(message = "{validation.not-null}")
    @NotBlank(message = "{validation.car.number.not-blank}")
    @Length(max = 15, message = "{validation.car.number.max-length}")
    private String number;

    @Schema(description = "Марка машины", example = "Honda")
    @NotNull(message = "{validation.not-null}")
    @NotBlank(message = "{validation.car.mark.not-blank}")
    @Length(max = 40, message = "{validation.car.mark.max-length}")
    private String mark;

    @Schema(description = "Цвет машины", example = "Белый")
    @NotBlank(message = "{validation.car.color.not-blank}")
    @Length(max = 40, message = "{validation.car.color.max-length}")
    @NotNull(message = "{validation.not-null}")
    private String color;
}
