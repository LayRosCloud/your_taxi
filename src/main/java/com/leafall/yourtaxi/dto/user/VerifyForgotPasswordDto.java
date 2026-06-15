package com.leafall.yourtaxi.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class VerifyForgotPasswordDto {
    @Schema(description = "Почта", example = "example@example.com")
    @NotNull(message = "{validation.not-null}")
    @NotBlank(message = "{validation.email.not-blank}")
    @Email(message = "{validation.email.invalid}")
    private String email;

    @Schema(description = "Код", example = "789213")
    @NotNull(message = "{validation.not-null}")
    @NotBlank(message = "{validation.code.not-blank}")
    @Length(min = 6, max = 6, message = "{validation.code.length}")
    private String code;
}
