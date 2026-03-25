package com.example.auth.interfaces.dto;

import java.time.LocalDateTime;

/**
 * 欢迎页响应 DTO
 */
public class WelcomeResponse {

    private String userId;
    private String username;
    private String email;
    private LocalDateTime createdAt;

    public WelcomeResponse(String userId, String username, String email, LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
