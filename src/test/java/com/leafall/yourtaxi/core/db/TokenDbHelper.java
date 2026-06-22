package com.leafall.yourtaxi.core.db;

import com.leafall.yourtaxi.dto.token.TokenHolder;
import com.leafall.yourtaxi.entity.UserEntity;
import com.leafall.yourtaxi.exception.NotFoundException;
import com.leafall.yourtaxi.repository.TokenRepository;
import com.leafall.yourtaxi.repository.UserRepository;
import com.leafall.yourtaxi.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import static com.leafall.yourtaxi.utils.TimeUtils.getExpiredDateFromUTC;

@Component
@RequiredArgsConstructor
public class TokenDbHelper {
    private final TokenService tokenService;

    public String generateAccessToken(UUID userId) {
        return tokenService.generateAccessToken(userId);
    }

    public String generateRefreshToken(UUID userId) {
        return tokenService.generateRefreshToken(userId);
    }
}
