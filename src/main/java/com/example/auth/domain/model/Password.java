package com.example.auth.domain.model;

import com.example.auth.domain.service.PasswordEncoder;

import java.util.Objects;

/**
 * 密码 - 值对象（存储已加密的密码哈希）
 */
public final class Password {

    private final String encodedValue;

    private Password(String encodedValue) {
        Objects.requireNonNull(encodedValue, "Password cannot be null");
        this.encodedValue = encodedValue;
    }

    public static Password encoded(String encodedValue) {
        return new Password(encodedValue);
    }

    public boolean matches(String rawPassword, PasswordEncoder encoder) {
        return encoder.matches(rawPassword, this.encodedValue);
    }

    public String getEncodedValue() {
        return encodedValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Password password)) return false;
        return Objects.equals(encodedValue, password.encodedValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(encodedValue);
    }
}
