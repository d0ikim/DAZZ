package com.dazz.backend.api.common;

import com.dazz.backend.domain.shared.BusinessException;
import com.dazz.backend.domain.shared.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.fail(new ErrorResponse(errorCode.getCode(), e.getMessage())));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity
                .status(400)
                .body(ApiResponse.fail(new ErrorResponse(ErrorCode.INVALID_INPUT.getCode(), message)));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<?>> handleMissingHeader(MissingRequestHeaderException e) {
        return ResponseEntity
                .status(400)
                .body(ApiResponse.fail(new ErrorResponse(
                        ErrorCode.INVALID_INPUT.getCode(),
                        "필수 헤더가 누락되었습니다: " + e.getHeaderName())));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity
                .status(400)
                .body(ApiResponse.fail(new ErrorResponse(ErrorCode.INVALID_INPUT.getCode(), message)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleUnexpected(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity
                .status(500)
                .body(ApiResponse.fail(new ErrorResponse(
                        ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                        ErrorCode.INTERNAL_SERVER_ERROR.getMessage())));
    }
}
