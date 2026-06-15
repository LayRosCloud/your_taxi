package com.leafall.yourtaxi.service;

import com.leafall.yourtaxi.dto.token.TokenHolder;
import com.leafall.yourtaxi.dto.user.*;
import com.leafall.yourtaxi.entity.CodeEntity;
import com.leafall.yourtaxi.entity.UserEntity;
import com.leafall.yourtaxi.entity.UserInfoEntity;
import com.leafall.yourtaxi.entity.enums.UserRole;
import com.leafall.yourtaxi.exception.BadRequestException;
import com.leafall.yourtaxi.exception.ConflictException;
import com.leafall.yourtaxi.exception.NotFoundException;
import com.leafall.yourtaxi.mapper.UserMapper;
import com.leafall.yourtaxi.repository.CodeRepository;
import com.leafall.yourtaxi.repository.UserInfoRepository;
import com.leafall.yourtaxi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.leafall.yourtaxi.utils.CodeUtils.generateCode;
import static com.leafall.yourtaxi.utils.SecurityUtils.getCurrentUserId;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository repository;
    private final TokenService tokenService;
    private final EncodingService encodingService;
    private final EmailService emailService;
    private final UserInfoRepository userInfoRepository;
    private final CodeRepository codeRepository;
    private final UserMapper mapper;

    public UserResponseDto getCurrentUser() {
        var user = repository.findById(getCurrentUserId())
                .orElseThrow(() -> new NotFoundException("user.error.not-found"));
        return mapper.mapToDto(user);
    }

    @Transactional
    public SuccessAuthDto signIn(SignInDto dto) {
        var user = repository.findByEmail(dto.getEmail().toLowerCase())
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
            log.warn("У пользователя {} пароли не совпадают", dto.getEmail());
            throw new BadRequestException("user.error.password-dont-match");
        }
        var userOptional = repository.findByEmail(dto.getEmail().toLowerCase());
        UserEntity user = null;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (user.getIsActive() && user.getDeletedAt() == null) {
                log.info("Пользователь {} существует, не удален и активирован при регистрации", dto.getEmail());
                throw new ConflictException("user.error.has-email");
            }
            log.info("Пользователь {} существует, но удален или активен", dto.getEmail());
        }
        if (user != null) {
            user.setDeletedAt(null);
            user.setPassword(encodingService.encode(dto.getPassword()));
            user.setFullName(dto.getFullName());
        } else {
            user = mapper.mapToEntity(dto);
            user.setEmail(dto.getEmail().toLowerCase());
            user.setPassword(encodingService.encode(dto.getPassword()));
            user.setRole(UserRole.USER);
            user.setIsActive(false);
        }
        user = repository.save(user);
        return sendIfNotActiveAccount(user);
    }

    @Transactional
    public UserResponseDto update(UserUpdateDto dto) {
        var user = repository.findById(getCurrentUserId())
                .orElseThrow(() -> new NotFoundException("user.error.not-found"));
        if (!user.getEmail().equalsIgnoreCase(dto.getEmail())) {
            var existUser = repository.findByEmail(dto.getEmail().toLowerCase()).isPresent();
            if (existUser) {
                throw new ConflictException("user.error.has-email");
            }
        }
        user.setEmail(dto.getEmail().toLowerCase());
        user.setFullName(dto.getFullName());
        if (user.getRole() == UserRole.EMPLOYEE) {
            UserInfoEntity info;
            if (user.getInfo() == null) {
                info = new UserInfoEntity();
                info.setUser(user);
            } else {
                info = user.getInfo();
            }
            info.setPhone(dto.getPhone());
            userInfoRepository.save(info);
        }
        var updatedUser = repository.save(user);
        return mapper.mapToDto(updatedUser);
    }

    @Transactional
    public UserResponseDto changePassword(UserChangePasswordDto dto) {
        var user = repository.findById(getCurrentUserId())
                .orElseThrow(() -> new NotFoundException("user.error.not-found"));
        if (!dto.getPassword().equals(dto.getRepeatPassword())) {
            log.warn("У пользователя {} пароли не совпадают", user.getEmail());
            throw new BadRequestException("user.error.password-dont-match");
        }
        user.setPassword(encodingService.encode(dto.getPassword()));
        var saved = repository.save(user);
        return mapper.mapToDto(saved);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean forgotPassword(ForgotPasswordDto dto) {
        var user = repository.findByEmail(dto.getEmail().toLowerCase()).orElse(null);
        if (user == null) {
            log.info("Пользователь не был найден. Не информируем пользователя в целях безопасности");
            return false;
        }
        log.info("Пользователь был найден. Отправляем код");
        var code = new CodeEntity();
        code.setUser(user);
        code.setCode(generateCode());
        code = codeRepository.save(code);
        emailService.sendForgotPassword(VerificationDto.builder()
                .code(code.getCode())
                .email(user.getEmail())
                .username(user.getFullName())
                .build()
        );
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public SuccessAuthDto verifyPasswordForgot(VerifyForgotPasswordDto dto) {
        var user = repository.findByEmail(dto.getEmail().toLowerCase())
                .orElseThrow(() -> new BadRequestException("code.error.invalid"));
        return verify(user, dto.getCode());
    }

    @Transactional
    public TokenHolder refresh(RefreshDto dto) {
        return tokenService.refresh(dto.getRefreshToken());
    }

    @Transactional
    public SuccessAuthDto verifyEmail(VerifyEmailDto dto) {
        var user = repository.findById(dto.getId())
                .orElseThrow(() -> new NotFoundException("user.error.not-found"));
        return verify(user, dto.getCode());
    }

    @Transactional
    public void logout(String token) {
        tokenService.logout(token);
    }

    private SuccessAuthDto verify(UserEntity user, String code) {
        var entity = codeRepository.findByCodeAndUser(code, user)
                .orElseThrow(() -> new BadRequestException("code.error.invalid"));
        user.setIsActive(true);
        var newUser = repository.save(user);
        codeRepository.delete(entity);
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
