package com.example.auth.domain.exception;

public final class AccountInactiveException extends AuthException {
    public AccountInactiveException() {
        super("Account is not activated", 403);
    }
}
