package com.leafall.yourtaxi.controller;

import com.leafall.yourtaxi.exception.annotation.ApiResponseBadRequest;
import com.leafall.yourtaxi.exception.annotation.ApiResponseConflict;
import com.leafall.yourtaxi.exception.annotation.ApiResponseUnauthorized;
import com.leafall.yourtaxi.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;

@RestController
@Tag(name = "Files", description = "Загрузка файлов")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileUploadService fileUploadService;

    @GetMapping("/v1/files/{folder}/{fileName}")
    @Operation(
            summary = "Скачать файл",
            description = "Обновить свою новую аватарку"

    )
    @ApiResponseBadRequest
    @ApiResponseConflict
    @ApiResponseUnauthorized
    @ApiResponse(responseCode = "200", description = "Поддерживает частичную загрузку (Range)")
    public ResponseEntity<Resource> downloadFile(@PathVariable String folder, @PathVariable String fileName,
                                             @Parameter(
                                                name = HttpHeaders.RANGE,
                                                in = ParameterIn.HEADER,
                                                description = "Диапазон байтов для частичной загрузки (например: bytes=0-1023)",
                                                example = "bytes=0-1023"
                                        )
                                        @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader
    ) {
        log.info("Начало загрузки файла {}/{}", folder, fileName);
        String contentType = "application/octet-stream";
        var resource = fileUploadService.loadAsResource(folder, fileName);
        try {
            contentType = Files.probeContentType(resource.getFile().toPath());
        } catch (IOException ignored) {

        }
        log.info("Файл найден {}/{}", folder, fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
