package com.leafall.yourtaxi.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ForgotPasswordDto {
    @Schema(description = "Почта", example = "example@example.com")
    @NotNull(message = "{validation.not-null}")
    @NotBlank(message = "{validation.email.not-blank}")
    @Email(message = "{validation.email.invalid}")
    private String email;
}
