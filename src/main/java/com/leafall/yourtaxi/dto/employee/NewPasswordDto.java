package com.leafall.yourtaxi.dto.employee;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NewPasswordDto {
    @NotNull(message = "{validation.not-null}")
    private String email;
    @NotNull(message = "{validation.not-null}")
    private String username;
    @NotNull(message = "{validation.not-null}")
    private String password;
}
