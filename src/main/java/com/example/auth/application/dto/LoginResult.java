package com.example.auth.application.dto;

/**
 * 登录结果 DTO（应用层出参）
 */
public record LoginResult(String token, String userId, String username, String email) {
}
