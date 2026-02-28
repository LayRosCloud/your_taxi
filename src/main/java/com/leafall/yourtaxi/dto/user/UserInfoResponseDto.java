package com.leafall.yourtaxi.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserInfoResponseDto {
    @Schema(description = "Телефон", example = "+7(900)555-33-22")
    @NotNull
    private String phone;
}
