package com.leafall.yourtaxi.controller;

import com.leafall.yourtaxi.dto.variable.VariableResponseDto;
import com.leafall.yourtaxi.dto.variable.VariableUpdateDto;
import com.leafall.yourtaxi.exception.annotation.ApiResponseUnauthorized;
import com.leafall.yourtaxi.service.VariableService;
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
@Tag(name = "Variables", description = "переменные в приложении")
public class VariableController {

    private final VariableService variableService;

    @GetMapping("/v1/variables")
    @Operation(
            summary = "Получить все переменные (только диспетчер)",
            description = "Получает все переменные сейчас `price`, `timer_seconds`, `big_order_from`"
    )
    @ApiResponseUnauthorized
    @ApiResponse(description = "Переменные получены", responseCode = "200")
    @PreAuthorize("hasAuthority('DISPATCHER')")
    public ResponseEntity<PaginationResponse<VariableResponseDto>> findAll(@ParameterObject PaginationParams params) {
        var variables = variableService.findAll(params);
        return new ResponseEntity<>(variables, HttpStatus.OK);
    }

    @GetMapping("/v1/variables/{id}")
    @Operation(
            summary = "Получить переменную по id (только диспетчер)",
            description = "Получить переменную"
    )
    @ApiResponseUnauthorized
    @ApiResponse(description = "Переменная получена", responseCode = "200")
    @PreAuthorize("hasAuthority('DISPATCHER')")
    public ResponseEntity<VariableResponseDto> findById(@PathVariable UUID id) {
        var variable = variableService.findById(id);
        return new ResponseEntity<>(variable, HttpStatus.OK);
    }

    @GetMapping("/v1/variables/key/{key}")
    @Operation(
            summary = "Получить переменную по ключу",
            description = "Получить переменную"
    )
    @ApiResponseUnauthorized
    @ApiResponse(description = "Переменная получена", responseCode = "200")
    public ResponseEntity<VariableResponseDto> findByKey(@PathVariable String key) {
        var variable = variableService.findByKey(key);
        return new ResponseEntity<>(variable, HttpStatus.OK);
    }

    @PutMapping("/v1/variables")
    @Operation(
            summary = "Обновить переменную (только диспетчер)",
            description = "Обновить переменную по id (только диспетчер)"
    )
    @ApiResponse(description = "Переменная обновлена", responseCode = "200")
    @ApiResponseUnauthorized
    @PreAuthorize("hasAuthority('DISPATCHER')")
    public ResponseEntity<VariableResponseDto> update(@RequestBody @Valid VariableUpdateDto dto) {
        var variable = variableService.update(dto);
        return new ResponseEntity<>(variable, HttpStatus.OK);
    }
}
