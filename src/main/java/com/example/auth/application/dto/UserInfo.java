package com.example.auth.application.dto;

import java.time.LocalDateTime;

/**
 * 用户信息查询结果 DTO（应用层出参）
 */
public record UserInfo(String userId, String username, String email, LocalDateTime createdAt) {
}
