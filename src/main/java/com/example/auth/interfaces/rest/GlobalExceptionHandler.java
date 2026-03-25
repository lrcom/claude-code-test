package com.example.auth.interfaces.rest;

import com.example.auth.domain.exception.AuthException;
import com.example.auth.interfaces.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 使用 JDK 21 pattern matching switch 统一处理 sealed AuthException 层级。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 参数校验失败（@Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationError(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(ApiResponse.error(message));
    }

    /**
     * 领域业务异常（JDK 21 pattern matching switch 穷举所有子类）
     */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthException(AuthException ex) {
        int status = switch (ex) {
            case com.example.auth.domain.exception.UserNotFoundException ignored          -> 404;
            case com.example.auth.domain.exception.EmailAlreadyExistsException ignored    -> 409;
            case com.example.auth.domain.exception.UsernameAlreadyExistsException ignored -> 409;
            case com.example.auth.domain.exception.InvalidPasswordException ignored       -> 400;
            case com.example.auth.domain.exception.AccountLockedException ignored         -> 403;
            case com.example.auth.domain.exception.AccountInactiveException ignored       -> 403;
        };
        return ResponseEntity.status(status).body(ApiResponse.error(status, ex.getMessage()));
    }

    /**
     * 未预期的服务端错误
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "Internal server error"));
    }
}

