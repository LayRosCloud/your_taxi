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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
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
    public ResponseEntity<PaginationResponse<UserResponseDto>> findAll(@ParameterObject PaginationParams pagination) {
        var users = service.findAllEmployees(pagination);
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
        var user = service.findById(id);
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
        var user = service.create(dto);
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
        var user = service.update(dto);
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
        var user = service.delete(id);
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
        service.deleteHard(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
