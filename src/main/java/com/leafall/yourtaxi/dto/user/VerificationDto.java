package com.leafall.yourtaxi.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerificationDto {
    @NotNull(message = "{validation.not-null}")
    private String email;
    @NotNull(message = "{validation.not-null}")
    private String username;
    @NotNull(message = "{validation.not-null}")
    private String code;
}
