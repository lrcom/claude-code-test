package com.example.auth.interfaces.rest;

import com.example.auth.application.command.LoginCommand;
import com.example.auth.application.command.RegisterCommand;
import com.example.auth.application.dto.LoginResult;
import com.example.auth.application.service.AuthApplicationService;
import com.example.auth.interfaces.dto.ApiResponse;
import com.example.auth.interfaces.dto.LoginRequest;
import com.example.auth.interfaces.dto.LoginResponse;
import com.example.auth.interfaces.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 认证接口控制器
 * <p>
 * 职责：接收 HTTP 请求，转换为应用层 Command，返回 HTTP 响应。
 * 不包含任何业务逻辑。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthApplicationService authApplicationService;

    public AuthController(AuthApplicationService authApplicationService) {
        this.authApplicationService = authApplicationService;
    }

    /**
     * 用户注册
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authApplicationService.register(
                new RegisterCommand(request.getUsername(), request.getEmail(), request.getPassword())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success());
    }

    /**
     * 用户登录
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = authApplicationService.login(
                new LoginCommand(request.getEmail(), request.getPassword())
        );
        LoginResponse response = new LoginResponse(
                result.token(), result.userId(), result.username(), result.email()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Token 校验（需要认证）
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<String>> me(
            @RequestAttribute(name = "userId", required = false) String userId) {
        // userId 由 JwtAuthenticationFilter 注入到 SecurityContext
        // 这里从 Principal 获取
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder
                        .getContext().getAuthentication();
        String currentUserId = auth != null ? (String) auth.getPrincipal() : "unknown";
        return ResponseEntity.ok(ApiResponse.success("Authenticated, userId: " + currentUserId));
    }
}
