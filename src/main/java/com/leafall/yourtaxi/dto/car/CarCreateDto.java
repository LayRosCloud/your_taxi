package com.leafall.yourtaxi.dto.car;

import com.leafall.yourtaxi.dto.user.UserResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;

@Data
public class CarCreateDto {
    @Schema(description = "Номер машины", example = "T222ZZ11 22")
    @NotNull
    @NotBlank(message = "Номер машины не может быть пустым")
    @Length(max = 15, message = "Номер машины может быть максимум 15 символов")
    private String number;

    @Schema(description = "Марка машины", example = "Honda")
    @NotNull
    @NotBlank(message = "Марка машины не может быть пустой")
    @Length(max = 40, message = "Марка машины может быть максимум 40 символов")
    private String mark;

    @Schema(description = "Цвет машины", example = "Белый")
    @NotBlank(message = "Цвет машины не может быть пустой")
    @Length(max = 40, message = "Цвет машины может быть максимум 40 символов")
    @NotNull
    private String color;
}
