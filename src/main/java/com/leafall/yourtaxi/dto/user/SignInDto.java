package com.leafall.yourtaxi.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;

@Data
public class SignInDto {
    @Schema(description = "Почта", example = "example@example.com")
    @NotNull
    @NotBlank(message = "Почта не может быть пустой")
    @Email(message = "Почта имеет неправильный формат")
    private String email;

    @Schema(description = "Пароль", example = "12345678")
    @NotNull
    @NotBlank(message = "Пароль не может быть пустым")
    @Length(min = 6, max = 40, message = "Пароль может быть от 6 до 40 символов")
    private String password;
}
