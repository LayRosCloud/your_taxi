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
    @NotNull
    private String email;
    @NotNull
    private String username;
    @NotNull
    private String code;
}
