package com.example.auth.domain.exception;

public final class UserNotFoundException extends AuthException {
    public UserNotFoundException(String userId) {
        super("User not found: " + userId, 404);
    }
}
