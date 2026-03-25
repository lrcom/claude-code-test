package com.example.auth.domain.service;

/**
 * 密码编码器 - 领域层接口（依赖倒置，实现在基础设施层）
 */
public interface PasswordEncoder {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
