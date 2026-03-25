package com.example.auth.infrastructure.security;

import com.example.auth.domain.service.PasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码编码器配置
 * <p>
 * 桥接 Spring Security BCrypt 与领域层 PasswordEncoder 接口
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 实现领域层 PasswordEncoder 接口（依赖倒置）
     */
    @Bean
    public PasswordEncoder passwordEncoder(BCryptPasswordEncoder bCryptPasswordEncoder) {
        return new PasswordEncoder() {
            @Override
            public String encode(String rawPassword) {
                return bCryptPasswordEncoder.encode(rawPassword);
            }

            @Override
            public boolean matches(String rawPassword, String encodedPassword) {
                return bCryptPasswordEncoder.matches(rawPassword, encodedPassword);
            }
        };
    }
}
