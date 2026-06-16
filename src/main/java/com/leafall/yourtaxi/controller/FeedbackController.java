package com.leafall.yourtaxi.controller;

import com.leafall.yourtaxi.dto.feedback.FeedbackCountDto;
import com.leafall.yourtaxi.dto.feedback.FeedbackCreateDto;
import com.leafall.yourtaxi.dto.feedback.FeedbackResponseDto;
import com.leafall.yourtaxi.exception.annotation.ApiResponseNotFound;
import com.leafall.yourtaxi.exception.annotation.ApiResponseUnauthorized;
import com.leafall.yourtaxi.service.FeedbackService;
import com.leafall.yourtaxi.utils.pagination.PaginationParams;
import com.leafall.yourtaxi.utils.pagination.PaginationResponse;
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

import static com.leafall.yourtaxi.utils.SecurityUtils.getCurrentUserId;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Feedbacks", description = "Обратная связь")
public class FeedbackController {
    private final FeedbackService feedbackService;

    @GetMapping("/v1/feedbacks")
    @Operation(
            summary = "Получить всю обратную связь"
    )
    @ApiResponse(description = "Получена обратная связь", responseCode = "200")
    @ApiResponseUnauthorized
    @PreAuthorize("hasAuthority('DISPATCHER')")
    public ResponseEntity<PaginationResponse<FeedbackResponseDto>> findAll(@ParameterObject PaginationParams params) {
        log.info("Начало получения обратной связи: {} пользователем {}", params, getCurrentUserId());
        var feedbacks = feedbackService.findAll(params);
        log.info("Получена обратная связь");
        return new ResponseEntity<>(feedbacks, HttpStatus.OK);
    }

    @GetMapping("/v1/feedbacks/count")
    @Operation(
            summary = "Получить количества непрочитанной обратной связи"
    )
    @ApiResponse(description = "Получена обратная связь", responseCode = "200")
    @ApiResponseUnauthorized
    @PreAuthorize("hasAuthority('DISPATCHER')")
    public ResponseEntity<FeedbackCountDto> countNotReadAt() {
        log.info("Начало получения количества обратной связи, которую не прочитали пользователем {}", getCurrentUserId());
        var count = feedbackService.count();
        log.info("Получена обратная связь");
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @GetMapping("/v1/feedbacks/{id}")
    @Operation(
            summary = "Получить обратную связь по id"
    )
    @ApiResponse(description = "Получена обратная связь", responseCode = "200")
    @ApiResponseUnauthorized
    @ApiResponseNotFound
    @PreAuthorize("hasAuthority('DISPATCHER')")
    public ResponseEntity<FeedbackResponseDto> findById(@PathVariable UUID id) {
        log.info("Начало получения обратной связи: {} пользователем {}", id, getCurrentUserId());
        var feedbacks = feedbackService.findById(id);
        log.info("Получена обратная связь");
        return new ResponseEntity<>(feedbacks, HttpStatus.OK);
    }

    @PostMapping("/v1/feedbacks")
    @Operation(
            summary = "Создать обратную связь"
    )
    @ApiResponse(description = "Создана обратная связь", responseCode = "201")
    @ApiResponseUnauthorized
    @ApiResponseNotFound
    public ResponseEntity<FeedbackResponseDto> create(@RequestBody @Valid FeedbackCreateDto dto) {
        log.info("Начало создания обратной связи: {} пользователем {}", dto, getCurrentUserId());
        var feedback = feedbackService.create(dto, getCurrentUserId());
        log.info("Создана обратная связь: {}", feedback.getId());
        return new ResponseEntity<>(feedback, HttpStatus.CREATED);
    }

    @PatchMapping("/v1/feedbacks/{id}/read")
    @Operation(
            summary = "Пометить, что прочитал обратную связь"
    )
    @ApiResponse(description = "Связь прочитана", responseCode = "200")
    @ApiResponseUnauthorized
    @ApiResponseNotFound
    @PreAuthorize("hasAuthority('DISPATCHER')")
    public ResponseEntity<FeedbackResponseDto> read(@PathVariable UUID id) {
        log.info("Начало чтения обратной связи: {} пользователем {}", id, getCurrentUserId());
        var feedback = feedbackService.read(id, getCurrentUserId());
        log.info("Создана обратная связь: {}", feedback.getId());
        return new ResponseEntity<>(feedback, HttpStatus.OK);
    }

}
