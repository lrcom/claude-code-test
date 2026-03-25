package com.example.auth.domain.exception;

public final class UsernameAlreadyExistsException extends AuthException {
    public UsernameAlreadyExistsException(String username) {
        super("Username already taken: " + username, 409);
    }
}
