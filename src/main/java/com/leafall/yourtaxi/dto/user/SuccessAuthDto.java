package com.leafall.yourtaxi.dto.user;

import com.leafall.yourtaxi.dto.token.TokenHolder;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SuccessAuthDto {
    @NotNull
    private TokenHolder tokens;

    @NotNull
    private UserResponseDto user;
}
