package com.leafall.yourtaxi.dto.variable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;

@Data
public class VariableUpdateDto {
    @Schema(
            description = "Id переменной",
            example = "9f76f9e4-c370-4066-bc21-1fffe0d4adcf"
    )
    @NotNull
    private UUID id;
    @Schema(
            description = "Значение переменной. Может быть строкой, числом или булевым значением.",
            example = "2"
    )
    @Length(max = 255)
    @NotNull
    private String value;
    @Schema(
            description = "Описание переменной.",
            example = "Цена за КМ"
    )
    @Length(max = 500)
    private String description;
}
