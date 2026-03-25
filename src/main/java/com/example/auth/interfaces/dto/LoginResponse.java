package com.example.auth.interfaces.dto;

/**
 * 登录响应 DTO
 */
public class LoginResponse {

    private String token;
    private String userId;
    private String username;
    private String email;
    private String tokenType = "Bearer";

    public LoginResponse(String token, String userId, String username, String email) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    public String getToken() {
        return token;
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

    public String getTokenType() {
        return tokenType;
    }
}
