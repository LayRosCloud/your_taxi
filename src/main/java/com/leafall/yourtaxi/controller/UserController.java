package com.leafall.yourtaxi.controller;

import com.leafall.yourtaxi.dto.token.TokenHolder;
import com.leafall.yourtaxi.dto.user.*;
import com.leafall.yourtaxi.exception.ApiError;
import com.leafall.yourtaxi.exception.BadRequestException;
import com.leafall.yourtaxi.exception.annotation.*;
import com.leafall.yourtaxi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static com.leafall.yourtaxi.utils.SecurityUtils.getCurrentUserId;

@RestController
@RequiredArgsConstructor
@Tag(name = "Users", description = "Пользователи")
@Slf4j
public class UserController {
    private final UserService service;

    @GetMapping("/v1/users/current")
    @Operation(summary = "Получить информацию о текущем юзере", description = "Получаете информацию из JWT токена")
    @ApiResponseUnauthorized
    @ApiResponse(responseCode = "200", description = "Получена информация о текущем пользователе")
    public ResponseEntity<UserDetailResponseDto> getCurrentUser() {
        log.info("Начало запроса на получение текущего юзера {}", getCurrentUserId());
        var user = service.getUser(getCurrentUserId());
        log.info("Пришло успешно тело id={} email={}", user.getId(), user.getEmail());
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/v1/users/{id}")
    @Operation(summary = "Получить информацию о пользователе", description = "Получить информацию о пользователе по id (только диспетчер) Без засекреченных данных")
    @ApiResponseUnauthorized
    @ApiResponse(responseCode = "200", description = "Получена информация о текущем пользователе")
    @PreAuthorize("hasAuthority('DISPATCHER')")
    public ResponseEntity<UserDetailResponseDto> getUserById(@PathVariable UUID id) {
        log.info("Начало запроса на получение юзера {}", id);
        var user = service.getUser(id);
        log.info("Пришло успешно тело id={} email={}", user.getId(), user.getEmail());
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/v1/users/signin")
    @Operation(
            summary = "Авторизация",
            description = "Авторизация аккаунта через email, password, если акк не активирован, авторизация не удастся",
            operationId = "v1_users_signin"
    )
    @ApiResponseBadRequest
    @ApiResponseNotFound
    @ApiResponse(responseCode = "200", description = "Успешная авторизация")
    public ResponseEntity<SuccessAuthDto> signIn(@RequestBody @Valid SignInDto dto) {
        log.info("Начало авторизации пользователя: email=\"{}\"", dto.getEmail());
        var user = service.signIn(dto);
        log.info("Пользователь \"{}\" успешно авторизован: id={}", dto.getEmail(), user.getUser().getId());
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
        log.info("Начало регистрации пользователя: email=\"{}\", fullname=\"{}\"", dto.getEmail(), dto.getFullName());
        var user = service.signUp(dto);
        log.info("Пользователь \"{}\" успешно зарегистрирован: id={}", dto.getEmail(), user.getUser().getId());
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PatchMapping(value = "/v1/users/current/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Загрузить новую аватарку",
            description = "Обновить свою новую аватарку"
    )
    @ApiResponseBadRequest
    @ApiResponseUnauthorized
    @ApiResponse(responseCode = "200", description = "Успешно обновился аватар")
    public ResponseEntity<UserResponseDto> uploadAvatar(@RequestParam(name = "file") MultipartFile file) {
        log.info("Начало обновления аватара пользователем {}", getCurrentUserId());
        var user = service.uploadAvatar(getCurrentUserId(),file);
        log.info("Аватар был обновлен {}", getCurrentUserId());
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/v1/users/forgot/password")
    @Operation(
            summary = "Забыли пароль",
            description = "Отправка письма на почту с кодом о восстановлении пароля"
    )
    @ApiResponseBadRequest
    @ApiResponseConflict
    @ApiResponse(responseCode = "204", description = "Успешно отправилось")
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordDto dto) {
        log.info("Начало восстановления пароль: email=\"{}\"", dto.getEmail());
        var result = service.forgotPassword(dto);
        log.info("Отправилось ли сообщение: {}", result);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/v1/users/forgot/password/verify")
    @Operation(
            summary = "Верификация кода для получения токенов",
            description = "Верификация кода чтобы получить токены для смены пароля"
    )
    @ApiResponseBadRequest
    @ApiResponseConflict
    @ApiResponse(responseCode = "200", description = "Успешно отправилось")
    public ResponseEntity<SuccessAuthDto> verifyForgotPassword(@RequestBody @Valid VerifyForgotPasswordDto dto) {
        log.info("Начало восстановления пароль: email=\"{}\" code=\"{}\"", dto.getEmail(), dto.getCode());
        var result = service.verifyPasswordForgot(dto);
        log.info("Успешно верифицировался пользователь id={}", result.getUser().getId());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping("/v1/users/current")
    @Operation(
            summary = "Обновление пользователя",
            description = "Обновление данных текущего пользователя"
    )
    @ApiResponseBadRequest
    @ApiResponseConflict
    @ApiResponseNotFound
    @ApiResponseUnauthorized
    @ApiResponse(responseCode = "200", description = "Успешное обновление")
    public ResponseEntity<UserDetailResponseDto> update(@RequestBody @Valid UserUpdateDto dto) {
        log.info("Начало изменения пользователя: id={} email=\"{}\", fullname=\"{}\"", getCurrentUserId(), dto.getEmail(), dto.getFullName());
        var user = service.update(dto);
        log.info("Пользователь \"{}\" успешно изменен: id={}", user.getEmail(), user.getId());
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PatchMapping("/v1/users/current/password")
    @Operation(
            summary = "Обновление пароля пользователя",
            description = "Обновление пароля текущего пользователя"
    )
    @ApiResponseBadRequest
    @ApiResponseConflict
    @ApiResponseNotFound
    @ApiResponseUnauthorized
    @ApiResponse(responseCode = "200", description = "Успешное обновление")
    public ResponseEntity<UserResponseDto> changePassword(@RequestBody @Valid UserChangePasswordDto dto) {
        log.info("Начало изменения пароля пользователя: id={}", getCurrentUserId());
        var user = service.changePassword(dto);
        log.info("Пользователь \"{}\" успешно изменен пароль: id={}", user.getEmail(), user.getId());
        return new ResponseEntity<>(user, HttpStatus.OK);
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
        log.info("Верификация аккаунта id={}, code={}", dto.getId(), dto.getCode());
        var user = service.verifyEmail(dto);
        log.info("Аккаунт верифицирован id={} isActive={}", user.getUser().getId(), user.getUser().getIsActive());
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
        log.info("Начало рефреша аккаунта {}", dto.getRefreshToken());
        var user = service.refresh(dto);
        log.info("Получена новая пара токенов для аккаунта");
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
    @DeleteMapping("/v1/users/current/avatar")
    @Operation(
            summary = "Удалить аватарку",
            description = "Удалить аватарку, если она есть"
    )
    @ApiResponseBadRequest
    @ApiResponseUnauthorized
    @ApiResponse(responseCode = "200", description = "Успешно удалился аватар")
    public ResponseEntity<UserResponseDto> deleteAvatar() {
        log.info("Начало удаления аватара пользователем {}", getCurrentUserId());
        var user = service.deleteAvatar(getCurrentUserId());
        log.info("Аватар был удален {}", getCurrentUserId());
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
    @PostMapping("/v1/users/logout")
    @Operation(
            summary = "Выход из аккаунта",
            description = "Удаляет пару токенов Refresh Access токенов"
    )
    @ApiResponseBadRequest
    @ApiResponseNotFound
    @ApiResponse(responseCode = "204", description = "Успешный выход")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> logout(@RequestHeader(name = "Authorization", required = false) String authToken) {
        if (authToken == null || !authToken.startsWith("Bearer ")) {
            log.info("Не был передан токен");
            throw new ApiError(401,"base.error.bad-request");
        }
        authToken = authToken.substring(7);
        log.info("Начало выхода из аккаунта");
        service.logout(authToken);
        log.info("Выход из аккаунта произошел успешно");
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
