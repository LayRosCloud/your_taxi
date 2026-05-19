package com.leafall.yourtaxi.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignInDto {
    @Schema(description = "Почта", example = "example@example.com")
    @NotNull(message = "{validation.not-null}")
    @NotBlank(message = "{validation.email.not-blank}")
    @Email(message = "{validation.email.invalid}")
    private String email;

    @Schema(description = "Пароль", example = "12345678")
    @NotNull(message = "{validation.not-null}")
    @NotBlank(message = "{validation.password.not-blank}")
    @Size(min = 6, max = 40, message = "{validation.password.size}")
    private String password;
}
