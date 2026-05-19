package com.leafall.yourtaxi.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserUpdateDto {
    @Schema(description = "Почта", example = "example@example.com")
    @NotNull(message = "{validation.not-null}")
    private String email;

    @Schema(description = "Как к вам обращаться", example = "Григорий")
    @NotNull(message = "{validation.not-null}")
    private String fullName;

    @Schema(description = "Телефон", example = "+79230009922")
    private String phone;
}
