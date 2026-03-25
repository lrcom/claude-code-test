package com.example.auth.domain.exception;

public final class EmailAlreadyExistsException extends AuthException {
    public EmailAlreadyExistsException(String email) {
        super("Email already registered: " + email, 409);
    }
}
