package com.example.auth.domain.exception;

public final class AccountLockedException extends AuthException {
    public AccountLockedException() {
        super("Account is locked", 403);
    }
}
