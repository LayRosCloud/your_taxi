package com.leafall.yourtaxi.service;

import com.leafall.yourtaxi.dto.token.TokenHolder;
import com.leafall.yourtaxi.entity.TokenEntity;
import com.leafall.yourtaxi.exceptions.ForbiddenException;
import com.leafall.yourtaxi.exceptions.NotFoundException;
import com.leafall.yourtaxi.repository.TokenRepository;
import com.leafall.yourtaxi.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.apache.coyote.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.util.*;

import static com.leafall.yourtaxi.utils.TimeUtils.getCurrentTimeFromUTC;
import static com.leafall.yourtaxi.utils.TimeUtils.getExpiredDateFromUTC;

@Service
public class TokenService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final SecretKey secretAccessKey;
    private final SecretKey secretRefreshKey;

    private final long accessTime;
    private final long refreshTime;

    public TokenService(
            @Autowired TokenRepository tokenRepository,
            @Autowired UserRepository userRepository,
            @Value("${jwt.access.secretKey}") String secretAccessKey,
            @Value("${jwt.refresh.secretKey}") String secretRefreshKey,
            @Value("${jwt.access.ttlMinutes}") long accessTime,
            @Value("${jwt.refresh.ttlMinutes}")long refreshTime
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.secretAccessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretAccessKey));
        this.secretRefreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretRefreshKey));
        this.accessTime = accessTime;
        this.refreshTime = refreshTime;
    }

    public String generateAccessToken(UUID userId) {
        Date expirationTime = getExpiredDateFromUTC(accessTime);

        var foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user.error.not-found"));

        var map = new HashMap<String, Object>();
        map.put("role", foundUser.getRole().getAuthority());
        return generateToken(foundUser.getId(), secretAccessKey, expirationTime, map);
    }

    public String generateRefreshToken(UUID userId) {
        Date expirationTime = getExpiredDateFromUTC(refreshTime);

        var foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user.error.not-found"));

        var map = new HashMap<String, Object>();
        map.put("role", foundUser.getRole().getAuthority());
        var refreshToken = generateToken(foundUser.getId(), secretRefreshKey, expirationTime, map);

        var toSave = new TokenEntity();
        toSave.setRefreshToken(refreshToken);
        toSave.setUser(foundUser);
        toSave.setExpiredAt(expirationTime.getTime());
        tokenRepository.save(toSave);

        return refreshToken;
    }
    public TokenHolder refresh(String token) {
        var entity = tokenRepository.findByRefreshToken(token)
                .orElseThrow(() -> new NotFoundException("token.error.not-found"));

        validateRefreshToken(entity.getRefreshToken());
        revokeToken(entity.getId());

        return TokenHolder.builder()
                .accessToken(generateAccessToken(entity.getUser().getId()))
                .refreshToken(generateRefreshToken(entity.getUser().getId()))
                .build();
    }

    public void validateAccessToken(String token) {
        validateToken(secretAccessKey, token);
    }
    public Claims getAccessClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(secretAccessKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    public void validateRefreshToken(String token) {
        validateToken(secretRefreshKey, token);
        var foundToken = tokenRepository.findByRefreshToken(token)
                .orElseThrow(() -> new NotFoundException("token.error.not-found"));

        if (foundToken.getRevokedAt() != null) {
            throw new ForbiddenException("token.error.used");
        }
    }

    private String generateToken(UUID userId, SecretKey secretKey, Date expirationTime, Map<String, Object> map) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .setExpiration(expirationTime)
                .setIssuedAt(new Date(getCurrentTimeFromUTC()))
                .setAudience("com.example.your_taxi")
                .addClaims(map)
                .signWith(secretKey)
                .compact();
    }

    private void validateToken(SecretKey key, String token) {
        Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    private void revokeToken(UUID tokenId) {
        var entity = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new NotFoundException("token.error.not-found"));

        entity.setRevokedAt(getCurrentTimeFromUTC());
        tokenRepository.save(entity);
    }
}
