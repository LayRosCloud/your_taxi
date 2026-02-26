package com.leafall.yourtaxi.dto.user;

import com.leafall.yourtaxi.dto.token.TokenHolder;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SuccessAuthDto {
    private TokenHolder tokens;
    private UserResponseDto user;
}
