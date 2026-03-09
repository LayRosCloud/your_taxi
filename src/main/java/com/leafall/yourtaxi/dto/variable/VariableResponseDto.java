package com.leafall.yourtaxi.dto.variable;

import com.leafall.yourtaxi.entity.enums.VariableType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class VariableResponseDto {
    @Schema(
            description = "Id переменной",
            example = "9f76f9e4-c370-4066-bc21-1fffe0d4adcf"
    )
    @NotNull
    private UUID id;
    @Schema(
            description = "Ключ переменной (всегда уникальный)",
            example = "price"
    )
    @NotNull
    private String key;
    @Schema(
            description = "Значение переменной. Может быть строкой, числом или булевым значением.",
            oneOf = {String.class, Double.class, Boolean.class},
            example = "2"
    )
    @NotNull
    private Object value;
    @Schema(
            description = "Описание переменной.",
            example = "Цена за КМ"
    )
    private String description;

    @Schema(
            description = "Тип переменной.",
            example = "NUMBER"
    )
    @NotNull
    private VariableType type;
}
