package com.leafall.yourtaxi.middleware;

import com.leafall.yourtaxi.dto.ErrorDto;
import com.leafall.yourtaxi.exception.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class ExceptionProvider {

    private final MessageSource messageSource;

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorDto> handleException(NoResourceFoundException exception, Locale locale) {
        log.warn(exception.getMessage(), exception);
        var message = messageSource.getMessage("base.error.not-found", new Object[]{}, locale);
        return new ResponseEntity<>(
                new ErrorDto(404, List.of(message)),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorDto> handleException(AuthorizationDeniedException exception, Locale locale) {
        log.warn(exception.getMessage(), exception);
        var message = messageSource.getMessage("base.error.forbidden", new Object[]{}, locale);
        return new ResponseEntity<>(
                new ErrorDto(403, List.of(message)),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleInvalidUUID(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors().stream()
                .map(x -> x.getField() + ": " + x.getDefaultMessage())
                .toList();
        return new ResponseEntity<>(new ErrorDto(400, errors), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleInvalidUUID(MethodArgumentTypeMismatchException ex) {
        if (ex.getRequiredType() != null && ex.getRequiredType().equals(UUID.class)) {
            return ResponseEntity.badRequest().body("Invalid UUID format: " + ex.getValue());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @ExceptionHandler(ApiError.class)
    public ResponseEntity<ErrorDto> handleException(ApiError exception, Locale locale) {
        var message = messageSource.getMessage(exception.getMessage(), new Object[]{}, locale);

        var logId = MDC.get(LoggerMiddleware.HEADER_CORRELATION_LOG_ID);
        log.warn("[{}] {}", logId, message);
        return new ResponseEntity<>(
                new ErrorDto(exception.getStatus(), List.of(message)),
                HttpStatusCode.valueOf(exception.getStatus())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleException(Exception exception, Locale locale) {
        var logId = MDC.get(LoggerMiddleware.HEADER_CORRELATION_LOG_ID);
        log.error("[{}] {}",logId, exception.getMessage(), exception);
        var message = messageSource.getMessage("base.error.internal-server-error", new Object[]{}, locale);
        return new ResponseEntity<>(
                new ErrorDto(500, List.of(message)),
                HttpStatusCode.valueOf(500)
        );
    }
}
