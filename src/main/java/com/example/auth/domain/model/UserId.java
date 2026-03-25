package com.example.auth.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * 用户ID - 值对象
 */
public final class UserId {

    private final String value;

    private UserId(String value) {
        Objects.requireNonNull(value, "UserId cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("UserId cannot be blank");
        }
        this.value = value;
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID().toString());
    }

    public static UserId of(String value) {
        return new UserId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserId userId)) return false;
        return Objects.equals(value, userId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
