package com.leafall.yourtaxi.exception.annotation;

import com.leafall.yourtaxi.dto.ErrorDto;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(responseCode = "404", description = "Запись не найдена",
        content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorDto.class)) )
public @interface ApiResponseNotFound {
}
