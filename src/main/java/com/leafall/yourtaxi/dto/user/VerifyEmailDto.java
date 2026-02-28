package com.leafall.yourtaxi.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;

@Data
public class VerifyEmailDto {
    @Schema(description = "Id", example = "9f76f9e4-c370-4066-bc21-1fffe0d4adcf")
    @NotNull
    private UUID id;
    @Schema(description = "Код", example = "789213")
    @NotNull
    @NotBlank(message = "Код не может быть пустым")
    @Length(min = 6, max = 6, message = "Код может быть 6 символов")
    private String code;
}
