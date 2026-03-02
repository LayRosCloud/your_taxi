package com.leafall.yourtaxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ErrorDto {
    @Schema(title = "Статус", description = "Статус ответа", example = "401")
    @NotNull
    private Integer status;
    @Schema(title = "Ошибки", description = "Все ошибки",
            example = "[\"Unauthorized\",\"Bad Request\",\"Forbidden\"]"
            )
    @NotNull
    private List<String> errors;

}
