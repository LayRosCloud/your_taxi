package com.leafall.yourtaxi.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UserResponseDto {
    @Schema(description = "Id", example = "9f76f9e4-c370-4066-bc21-1fffe0d4adcf")
    @NotNull
    private UUID id;

    @Schema(description = "Почта", example = "example@example.com")
    @NotNull
    private String email;

    @Schema(description = "Как к вам обращаться", example = "Григорий")
    @NotNull
    private String fullName;
    @Schema(description = "Роль", example = "USER")
    @NotNull
    private String role;
    @Schema(description = "Активирована ли почта", example = "true")
    @NotNull
    private Boolean isActive;
    @Schema(description = "Дата создания аккаунта UTC", example = "1766091600000")
    @NotNull
    private Long createdAt;
    @Schema(description = "Доп инфа о юзере")
    private UserInfoResponseDto info;
}
