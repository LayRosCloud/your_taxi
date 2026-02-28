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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Employees")
public class EmployeeController {

    private final EmployeeService service;

    @GetMapping("/v1/employees")
    @ApiResponseUnauthorized
    @ApiResponse(responseCode = "200", description = "Получены все сотрудники")
    public ResponseEntity<PaginationResponse<UserResponseDto>> findAll(@ParameterObject PaginationParams pagination) {
        var users = service.findAllEmployees(pagination);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/v1/employees/{id}")
    @ApiResponseBadRequest
    @ApiResponseUnauthorized
    @ApiResponseNotFound
    @ApiResponse(responseCode = "200", description = "Получен сотрудник")
    public ResponseEntity<UserResponseDto> findById(@PathVariable UUID id) {
        var user = service.findById(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/v1/employees")
    @ApiResponseBadRequest
    @ApiResponseUnauthorized
    @ApiResponse(responseCode = "201", description = "Создан сотрудник")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserResponseDto> create(@RequestBody @Valid EmployeeCreateDto dto) {
        var user = service.create(dto);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PutMapping("/v1/employees")
    @ApiResponseBadRequest
    @ApiResponseUnauthorized
    @ApiResponseNotFound
    @ApiResponse(responseCode = "200", description = "Обновлен сотрудник")
    public ResponseEntity<UserResponseDto> update(@RequestBody @Valid EmployeeUpdateDto dto) {
        var user = service.update(dto);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @DeleteMapping("/v1/employees/{id}")
    @ApiResponseBadRequest
    @ApiResponseUnauthorized
    @ApiResponseNotFound
    @ApiResponse(responseCode = "200", description = "Сотрудник отмечен на удаление")
    public ResponseEntity<UserResponseDto> deleteSoft(@PathVariable UUID id) {
        var user = service.delete(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @DeleteMapping("/v1/employees/{id}/hard")
    @ApiResponseBadRequest
    @ApiResponseUnauthorized
    @ApiResponseNotFound
    @ApiResponse(responseCode = "204", description = "Сотрудник и вся связанная инфа удалена")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteHard(@PathVariable UUID id) {
        service.deleteHard(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
