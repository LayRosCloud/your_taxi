package com.leafall.yourtaxi.controller;

import com.leafall.yourtaxi.dto.token.TokenHolder;
import com.leafall.yourtaxi.dto.user.*;
import com.leafall.yourtaxi.exception.BadRequestException;
import com.leafall.yourtaxi.exception.annotation.*;
import com.leafall.yourtaxi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Users", description = "Пользователи")
public class UserController {
    private final UserService service;

    @GetMapping("/v1/users/current")
    @Operation(summary = "Получить информацию о текущем юзере", description = "Получаете информацию из JWT токена")
    @ApiResponseUnauthorized
    @ApiResponse(responseCode = "200", description = "Получена информация о текущем пользователе")
    public ResponseEntity<UserResponseDto> getCurrentUser() {
        var user = service.getCurrentUser();
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
    @PostMapping("/v1/users/signin")
    @Operation(
            summary = "Авторизация",
            description = "Авторизация аккаунта через email, password, если акк не активирован, авторизация не удастся"
    )
    @ApiResponseBadRequest
    @ApiResponseNotFound
    @ApiResponse(responseCode = "200", description = "Успешная авторизация")
    public ResponseEntity<SuccessAuthDto> signIn(@RequestBody @Valid SignInDto dto) {
        var user = service.signIn(dto);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/v1/users/signup")
    @Operation(
            summary = "Регистрация",
            description = "Если акк удален или не активирован, ошибка не выдастся"
    )
    @ApiResponseBadRequest
    @ApiResponseConflict
    @ApiResponse(responseCode = "201", description = "Успешная регистрация")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<SuccessAuthDto> signUp(@RequestBody @Valid SignUpDto dto) {
        var user = service.signUp(dto);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PostMapping("/v1/users/verify")
    @Operation(
            summary = "Верификация аккаунта",
            description = "Подтверждение почты с письма на почты"
    )
    @ApiResponseBadRequest
    @ApiResponseNotFound
    @ApiResponse(responseCode = "200", description = "Успешная верификация")
    public ResponseEntity<SuccessAuthDto> verify(@RequestBody @Valid VerifyEmailDto dto) {
        var user = service.verifyEmail(dto);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/v1/users/tokens/refresh")
    @Operation(
            summary = "Обновление сессии",
            description = "Дает новую пару JWT токенов"
    )
    @ApiResponseNotFound
    @ApiResponse(responseCode = "200", description = "Успешное обновление токена")
    public ResponseEntity<TokenHolder> refresh(@RequestBody @Valid RefreshDto dto) {
        var user = service.refresh(dto);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/v1/users/logout")
    @Operation(
            summary = "Выход из аккаунта",
            description = "Удаляет пару токенов Refresh Access токенов"
    )
    @ApiResponseBadRequest
    @ApiResponseNotFound
    @ApiResponseForbidden
    @ApiResponse(responseCode = "204", description = "Успешный выход")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> logout(@RequestHeader(name = "Authorization") String authToken) {
        if (authToken == null || !authToken.startsWith("Bearer ")) {
            throw new BadRequestException("base.error.bad-request");
        }
        authToken = authToken.substring(7);

        service.logout(authToken);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
