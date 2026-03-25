package com.example.auth.interfaces.rest;

import com.example.auth.application.dto.UserInfo;
import com.example.auth.application.service.AuthApplicationService;
import com.example.auth.interfaces.dto.ApiResponse;
import com.example.auth.interfaces.dto.WelcomeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口控制器
 * <p>
 * 职责：提供登录后的用户相关接口，所有端点均需 JWT 认证。
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final AuthApplicationService authApplicationService;

    public UserController(AuthApplicationService authApplicationService) {
        this.authApplicationService = authApplicationService;
    }

    /**
     * 获取欢迎页数据（需要认证）
     * GET /api/user/welcome
     */
    @GetMapping("/welcome")
    public ResponseEntity<ApiResponse<WelcomeResponse>> welcome() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) auth.getPrincipal();

        UserInfo userInfo = authApplicationService.getUserById(userId);
        WelcomeResponse response = new WelcomeResponse(
                userInfo.userId(), userInfo.username(), userInfo.email(), userInfo.createdAt());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
