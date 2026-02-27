package com.leafall.yourtaxi.service;

import com.leafall.yourtaxi.dto.token.TokenHolder;
import com.leafall.yourtaxi.dto.user.*;
import com.leafall.yourtaxi.entity.CodeEntity;
import com.leafall.yourtaxi.entity.UserEntity;
import com.leafall.yourtaxi.entity.enums.UserRole;
import com.leafall.yourtaxi.exceptions.BadRequestException;
import com.leafall.yourtaxi.exceptions.NotFoundException;
import com.leafall.yourtaxi.mapper.UserMapper;
import com.leafall.yourtaxi.repository.CodeRepository;
import com.leafall.yourtaxi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.leafall.yourtaxi.utils.CodeUtils.generateCode;
import static com.leafall.yourtaxi.utils.SecurityUtils.getCurrentUserId;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final TokenService tokenService;
    private final EncodingService encodingService;
    private final EmailService emailService;
    private final CodeRepository codeRepository;
    private final UserMapper mapper;

    public UserResponseDto getCurrentUser() {
        var user = repository.findById(getCurrentUserId())
                .orElseThrow(() -> new NotFoundException("user.error.not-found"));
        return mapper.mapToDto(user);
    }

    @Transactional
    public SuccessAuthDto signIn(SignInDto dto) {
        var user = repository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new BadRequestException("user.error.bad-email-or-pass"));

        if (user.getDeletedAt() != null) {
            throw new BadRequestException("user.error.bad-email-or-pass");
        }

        if (!encodingService.isMatch(dto.getPassword(), user.getPassword())) {
            throw new BadRequestException("user.error.bad-email-or-pass");
        }

        return sendIfNotActiveAccount(user);
    }

    @Transactional
    public SuccessAuthDto signUp(SignUpDto dto) {
        if (!dto.getPassword().equals(dto.getRepeatPassword())) {
            throw new BadRequestException("user.error.password-dont-match");
        }
        var userOptional = repository.findByEmail(dto.getEmail());
        UserEntity user = null;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (user.getIsActive() && user.getDeletedAt() == null) {
                throw new BadRequestException("user.error.has-email");
            }
        }
        if (user != null) {
            user.setDeletedAt(null);
            user.setPassword(encodingService.encode(dto.getPassword()));
            user.setFullName(dto.getFullName());
        } else {
            user = new UserEntity();
            user.setFullName(dto.getFullName());
            user.setPassword(encodingService.encode(dto.getPassword()));
            user.setEmail(dto.getEmail());
            user.setRole(UserRole.USER);
            user.setIsActive(false);
        }
        user = repository.save(user);
        return sendIfNotActiveAccount(user);
    }

    @Transactional
    public TokenHolder refresh(RefreshDto dto) {
        return tokenService.refresh(dto.getRefreshToken());
    }

    @Transactional
    public SuccessAuthDto verifyEmail(VerifyEmailDto dto) {
        var user = repository.findById(dto.getId())
                .orElseThrow(() -> new NotFoundException("user.error.not-found"));
        var code = codeRepository.findByCodeAndUser(dto.getCode(), user)
                .orElseThrow(() -> new BadRequestException("code.error.invalid"));
        user.setIsActive(true);
        var newUser = repository.save(user);
        codeRepository.delete(code);
        var accessToken = tokenService.generateAccessToken(user.getId());
        var refreshToken = tokenService.generateRefreshToken(user.getId());
        return SuccessAuthDto.builder()
                .tokens(TokenHolder.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build())
                .user(mapper.mapToDto(newUser))
                .build();
    }

    @Transactional
    public void logout(String token) {
        tokenService.logout(token);
    }

    private SuccessAuthDto sendIfNotActiveAccount(UserEntity user) {
        if (!user.getIsActive()) {
            var code = new CodeEntity();
            code.setUser(user);
            code.setCode(generateCode());
            code = codeRepository.save(code);
            emailService.sendVerificationMessage(VerificationDto.builder()
                    .code(code.getCode())
                    .email(user.getEmail())
                    .username(user.getFullName())
                    .build()
            );
            return SuccessAuthDto.builder()
                    .user(mapper.mapToDto(user))
                    .build();
        }
        var accessToken = tokenService.generateAccessToken(user.getId());
        var refreshToken = tokenService.generateRefreshToken(user.getId());
        return SuccessAuthDto.builder()
                .tokens(TokenHolder.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build())
                .user(mapper.mapToDto(user))
                .build();
    }
}
