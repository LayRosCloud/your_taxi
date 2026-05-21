package com.leafall.yourtaxi.controller;

import com.leafall.yourtaxi.dto.employee.EmployeeCreateDto;
import com.leafall.yourtaxi.dto.employee.EmployeeUpdateDto;
import com.leafall.yourtaxi.dto.user.UserResponseDto;
import com.leafall.yourtaxi.exception.annotation.ApiResponseBadRequest;
import com.leafall.yourtaxi.exception.annotation.ApiResponseNotFound;
import com.leafall.yourtaxi.exception.annotation.ApiResponseUnauthorized;
import com.leafall.yourtaxi.service.EmployeeService;
import com.leafall.yourtaxi.utils.pagination.PaginationParams;
import com.leafall.yourtaxi.utils.pagination.PaginationResponse;
import com.leafall.yourtaxi.utils.request.EmployeeRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Employees", description = "Сотрудники")
public class EmployeeController {

    private final EmployeeService service;

    @GetMapping("/v1/employees")
    @Operation(
            summary = "Получить сотрудников",
            description = "Получает всех сотрудников (только диспетчер)"
    )
    @ApiResponseUnauthorized
    @ApiResponse(responseCode = "200", description = "Получены все сотрудники")
    @PreAuthorize("hasAuthority('DISPATCHER')")
    public ResponseEntity<PaginationResponse<UserResponseDto>> findAll(@ParameterObject EmployeeRequestDto dto) {
        log.info("Начало получения сотрудников: {}", dto);
        var users = service.findAllEmployees(dto);
        log.info("Получено {} сотрудников", users.cursor().getTotal());
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/v1/employees/{id}")
    @Operation(
            summary = "Получить сотрудника",
            description = "Получает сотрудника (только диспетчер)"
    )
    @ApiResponseBadRequest
    @ApiResponseUnauthorized
    @ApiResponseNotFound
    @ApiResponse(responseCode = "200", description = "Получен сотрудник")
    @PreAuthorize("hasAuthority('DISPATCHER')")
    public ResponseEntity<UserResponseDto> findById(@PathVariable UUID id) {
        log.info("Начало получения сотрудника по id: {}", id);
        var user = service.findById(id);
        log.info("Получен сотрудник {}", user.getFullName());
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/v1/employees")
    @Operation(
            summary = "Создать нового сотрудника",
            description = "Создать сотрудника (только диспетчер)"
    )
    @ApiResponseBadRequest
    @ApiResponseUnauthorized
    @ApiResponse(responseCode = "201", description = "Создан сотрудник")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('DISPATCHER')")
    public ResponseEntity<UserResponseDto> create(@RequestBody @Valid EmployeeCreateDto dto) {
        log.info("Начало создания сотрудника: fullname={} email={}", dto.getFullName(), dto.getEmail());
        var user = service.create(dto);
        log.info("Сотрудник создан id={}", user.getId());
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PutMapping("/v1/employees")
    @Operation(
            summary = "Обновить сотрудника",
            description = "Обновить сотрудника (только диспетчер)"
    )
    @ApiResponseBadRequest
    @ApiResponseUnauthorized
    @ApiResponseNotFound
    @ApiResponse(responseCode = "200", description = "Обновлен сотрудник")
    @PreAuthorize("hasAuthority('DISPATCHER')")
    public ResponseEntity<UserResponseDto> update(@RequestBody @Valid EmployeeUpdateDto dto) {
        log.info("Начало обновления сотрудника: id={}", dto.getId());
        var user = service.update(dto);
        log.info("Сотрудник {} обновлен email={}", dto.getFullName(), dto.getEmail());
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @DeleteMapping("/v1/employees/{id}")
    @Operation(
            summary = "Удалить сотрудника (мягко)",
            description = "Удалить сотрудника, с возможностью восстановления (только диспетчер)"
    )
    @ApiResponseBadRequest
    @ApiResponseUnauthorized
    @ApiResponseNotFound
    @ApiResponse(responseCode = "200", description = "Сотрудник отмечен на удаление")
    @PreAuthorize("hasAuthority('DISPATCHER')")
    public ResponseEntity<UserResponseDto> deleteSoft(@PathVariable UUID id) {
        log.info("Начало мягкого удаления сотрудника: id={}", id);
        var user = service.delete(id);
        log.info("Сотрудник мягко удален");
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @DeleteMapping("/v1/employees/{id}/hard")
    @Operation(
            summary = "Удалить сотрудника (жестко)",
            description = "Удалить сотрудника без возможности восстановления (только диспетчер)"
    )
    @ApiResponseBadRequest
    @ApiResponseUnauthorized
    @ApiResponseNotFound
    @ApiResponse(responseCode = "204", description = "Сотрудник и вся связанная инфа удалена")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('DISPATCHER')")
    public ResponseEntity<Void> deleteHard(@PathVariable UUID id) {
        log.info("Начало жесткого удаления сотрудника: id={}", id);
        service.deleteHard(id);
        log.info("Сотрудник удален навсегда");
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
