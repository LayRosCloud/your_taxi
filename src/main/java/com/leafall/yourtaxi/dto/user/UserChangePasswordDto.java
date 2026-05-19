package com.leafall.yourtaxi.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserChangePasswordDto {
    @Schema(description = "Пароль", example = "12345678")
    @NotNull(message = "{validation.not-null}")
    @NotBlank(message = "{validation.password.not-blank}")
    @Size(min = 6, max = 40, message = "{validation.password.size}")
    private String password;

    @Schema(description = "повторите пароль", example = "12345678")
    @NotNull(message = "{validation.not-null}")
    @NotBlank(message = "{validation.password.not-blank}")
    @Size(min = 6, max = 40, message = "{validation.password.size}")
    private String repeatPassword;
}
