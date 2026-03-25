package com.example.auth.application.port;

/**
 * Token 服务端口（应用层定义，基础设施层实现）
 */
public interface TokenService {

    String generateToken(String userId, String username);

    boolean validateToken(String token);

    String getUserIdFromToken(String token);

    String getUsernameFromToken(String token);
}
