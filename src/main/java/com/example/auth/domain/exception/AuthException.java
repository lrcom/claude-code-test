package com.example.auth.domain.exception;

/**
 * 领域异常基类（JDK 21 sealed class）
 * <p>
 * 所有子类均为 permits 列表中的 final 类，确保异常层级封闭、可穷举。
 * GlobalExceptionHandler 通过 pattern matching switch 统一处理。
 */
public sealed abstract class AuthException extends RuntimeException
        permits UserNotFoundException,
                EmailAlreadyExistsException,
                UsernameAlreadyExistsException,
                InvalidPasswordException,
                AccountLockedException,
                AccountInactiveException {

    private final int httpStatus;

    protected AuthException(String message, int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public int httpStatus() {
        return httpStatus;
    }
}
