package com.leafall.yourtaxi.controller;

import com.leafall.yourtaxi.dto.generatedFiles.GeneratedFileResponseDto;
import com.leafall.yourtaxi.exception.annotation.ApiResponseBadRequest;
import com.leafall.yourtaxi.exception.annotation.ApiResponseNotFound;
import com.leafall.yourtaxi.exception.annotation.ApiResponseUnauthorized;
import com.leafall.yourtaxi.service.GenerateFileService;
import com.leafall.yourtaxi.utils.pagination.PaginationParams;
import com.leafall.yourtaxi.utils.pagination.PaginationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.UUID;

import static com.leafall.yourtaxi.utils.SecurityUtils.getCurrentUserId;

@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Generated Files")
public class GeneratedFileController {
    private final GenerateFileService service;

    @GetMapping("/v1/orders/reports")
    @Operation(
            summary = "Получения файлов (-рапортов)",
            description = "Получения файлов (-рапортов) (только диспетчер)"
    )
    @ApiResponseUnauthorized
    @ApiResponse(description = "Репорт получен", responseCode = "200")
    @PreAuthorize("hasAuthority('DISPATCHER')")
    public ResponseEntity<PaginationResponse<GeneratedFileResponseDto>> findAll(@ParameterObject PaginationParams params) {
        log.info("Начало получения отчетов: {}", params);
        var result = service.findAll(params);
        log.info("Отчеты получены");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/v1/orders/reports/{id}")
    @Operation(
            summary = "Получения файла по id",
            description = "Получения файла по id (только диспетчер)"
    )
    @ApiResponseUnauthorized
    @ApiResponse(description = "Репорт получен", responseCode = "200")
    @PreAuthorize("hasAuthority('DISPATCHER')")
    public ResponseEntity<GeneratedFileResponseDto> findById(@PathVariable UUID id) {
        log.info("Начало получения {} отчета", id);
        var result = service.findById(id);
        log.info("Отчет получен");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/v1/orders/reports/{id}/download")
    @Operation(
            summary = "Скачивание файла по id",
            description = "Скачивание файла по id (только диспетчер)"
    )
    @ApiResponseUnauthorized
    @ApiResponse(description = "Репорт получен", responseCode = "200", content = @Content(
            mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            schema = @Schema(type = "string", format = "binary")
    ))
    @ApiResponse(description = "Отчет еще генерируется", responseCode = "202", content = @Content(
            mediaType = "application/json"
    ))
    @PreAuthorize("hasAuthority('DISPATCHER')")
    public ResponseEntity<Resource> download(@PathVariable UUID id) {
        log.info("Начало скачивания файла {}", id);
        var result = service.download(id);
        log.info("Файл {} получен", result.getFilename());
        if (result.getResource() == null) {
            return ResponseEntity.status(202)
                    .header(HttpHeaders.RETRY_AFTER, "180")
                    .body(null);
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", result.getFilename()))
                .body(result.getResource());
    }

    @PostMapping("/v1/orders/generate/report/excel")
    @Operation(
            summary = "Сгенерировать Excel отчет с даты по дату",
            description = "Сгенерировать Excel отчет с даты по дату (только диспетчер) Асинхронная операция"
    )
    @ApiResponseBadRequest
    @ApiResponseUnauthorized
    @ApiResponseNotFound
    @ApiResponse(description = "Водитель установлен", responseCode = "200")
    @PreAuthorize("hasAuthority('DISPATCHER')")
    public ResponseEntity<GeneratedFileResponseDto> generateExcel(@RequestParam String fromDate, @RequestParam String toDate) {
        log.info("Начало генерации отчета по датам: {} {}", fromDate, toDate);
        var order = service.generateExcel(getCurrentUserId(), Date.valueOf(fromDate), Date.valueOf(toDate));
        log.info("Запрос на генерацию установлена");
        return new ResponseEntity<>(order, HttpStatus.OK);
    }
}
