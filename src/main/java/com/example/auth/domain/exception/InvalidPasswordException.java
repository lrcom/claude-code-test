package com.example.auth.domain.exception;

public final class InvalidPasswordException extends AuthException {
    public InvalidPasswordException() {
        super("Invalid password", 400);
    }
}
