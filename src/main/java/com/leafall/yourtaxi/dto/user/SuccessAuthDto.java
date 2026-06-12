package com.leafall.yourtaxi.dto.user;

import com.leafall.yourtaxi.dto.token.TokenHolder;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SuccessAuthDto {
    @NotNull(message = "{validation.not-null}")
    private TokenHolder tokens;

    @NotNull(message = "{validation.not-null}")
    private UserResponseDto user;
}
