package com.example.auth;

import com.example.auth.application.command.RegisterCommand;
import com.example.auth.application.service.AuthApplicationService;
import com.example.auth.domain.model.User;
import com.example.auth.domain.model.UserStatus;
import com.example.auth.domain.repository.UserRepository;
import com.example.auth.domain.service.PasswordEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 登录接口闭环测试
 *
 * 测试范围：
 *   [E2E]  TC01 — 注册 → 登录 → 携带 Token 访问受保护接口（完整闭环）
 *   [成功]  TC02 — 正常用户登录，响应字段完整性校验
 *   [失败]  TC03 — 邮箱不存在 → 404
 *   [失败]  TC04 — 密码错误   → 400
 *   [失败]  TC05 — 账号锁定   → 403
 *   [失败]  TC06 — 账号未激活 → 403
 *   [参数]  TC07 — 邮箱为空   → 400 校验
 *   [参数]  TC08 — 邮箱格式非法 → 400 校验
 *   [参数]  TC09 — 密码为空   → 400 校验
 *   [参数]  TC10 — 请求体字段全缺失 → 400
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("登录接口闭环测试")
class LoginApiTest {

    private static final String LOGIN_URL    = "/api/auth/login";
    private static final String REGISTER_URL = "/api/auth/register";
    private static final String ME_URL       = "/api/auth/me";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AuthApplicationService authService;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // =========================================================================
    // TC01  [E2E 闭环]  注册 → 登录 → 携带 Token 访问 /me
    // =========================================================================

    @Test
    @DisplayName("TC01 [E2E 闭环] 注册 → 登录 → 携带 Token 访问受保护接口")
    void tc01_fullCycle_register_login_accessProtectedApi() throws Exception {
        String email    = "tc01@example.com";
        String password = "Test@1234";

        // Step 1: 注册
        String registerBody = objectMapper.writeValueAsString(
                Map.of("username", "tc01user", "email", email, "password", password));
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated());

        // Step 2: 登录，拿到 token
        String loginBody = objectMapper.writeValueAsString(
                Map.of("email", email, "password", password));
        MvcResult loginResult = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value(not(emptyString())))
                .andReturn();

        String token = objectMapper.readTree(
                loginResult.getResponse().getContentAsString())
                .at("/data/token").asText();

        // Step 3: 携带 Token 访问 /me，验证认证信息
        mockMvc.perform(get(ME_URL)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(containsStringIgnoringCase("authenticated")));
    }

    // =========================================================================
    // TC02  [成功]  登录成功 — 响应字段完整性
    // =========================================================================

    @Test
    @DisplayName("TC02 [成功] 登录成功 — 响应体包含 token / userId / username / email")
    void tc02_login_success_responseContainsAllFields() throws Exception {
        String email    = "tc02@example.com";
        String password = "Pass@5678";
        authService.register(new RegisterCommand("tc02user", email, password));

        String body = objectMapper.writeValueAsString(
                Map.of("email", email, "password", password));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value(not(emptyString())))
                .andExpect(jsonPath("$.data.userId").value(not(emptyString())))
                .andExpect(jsonPath("$.data.username").value("tc02user"))
                .andExpect(jsonPath("$.data.email").value(email));
    }

    // =========================================================================
    // TC03  [失败]  邮箱不存在 → 404
    // =========================================================================

    @Test
    @DisplayName("TC03 [失败] 邮箱不存在 — 返回 HTTP 404，code=404，message 含 not found")
    void tc03_login_userNotFound_returns404() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("email", "ghost@example.com", "password", "anyPassword"));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(containsStringIgnoringCase("not found")));
    }

    // =========================================================================
    // TC04  [失败]  密码错误 → 400
    // =========================================================================

    @Test
    @DisplayName("TC04 [失败] 密码错误 — 返回 HTTP 400，code=400，message=Invalid password")
    void tc04_login_wrongPassword_returns400() throws Exception {
        String email = "tc04@example.com";
        authService.register(new RegisterCommand("tc04user", email, "Correct@9999"));

        String body = objectMapper.writeValueAsString(
                Map.of("email", email, "password", "WrongPassword!"));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Invalid password"));
    }

    // =========================================================================
    // TC05  [失败]  账号锁定 → 403
    // =========================================================================

    @Test
    @DisplayName("TC05 [失败] 账号锁定 — 返回 HTTP 403，code=403，message=Account is locked")
    void tc05_login_accountLocked_returns403() throws Exception {
        String email    = "tc05@example.com";
        String password = "Test@1234";
        authService.register(new RegisterCommand("tc05user", email, password));

        // 锁定账号
        User user = userRepository.findByEmail(email).orElseThrow();
        user.lock();
        userRepository.save(user);

        String body = objectMapper.writeValueAsString(
                Map.of("email", email, "password", password));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("Account is locked"));
    }

    // =========================================================================
    // TC06  [失败]  账号未激活 → 403
    // =========================================================================

    @Test
    @DisplayName("TC06 [失败] 账号未激活 — 返回 HTTP 403，code=403，message=Account is not activated")
    void tc06_login_accountInactive_returns403() throws Exception {
        String email    = "tc06@example.com";
        String password = "Test@1234";

        // 直接构造 INACTIVE 状态用户（绕过 register 工厂方法，User.register 默认 ACTIVE）
        User inactiveUser = User.reconstitute(
                UUID.randomUUID().toString(),
                "tc06user",
                email,
                passwordEncoder.encode(password),
                UserStatus.INACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        userRepository.save(inactiveUser);

        String body = objectMapper.writeValueAsString(
                Map.of("email", email, "password", password));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("Account is not activated"));
    }

    // =========================================================================
    // TC07  [参数校验]  邮箱为空 → 400
    // =========================================================================

    @Test
    @DisplayName("TC07 [参数] 邮箱为空 — 返回 HTTP 400 校验错误")
    void tc07_login_blankEmail_returns400() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("email", "", "password", "somePassword"));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    // =========================================================================
    // TC08  [参数校验]  邮箱格式非法 → 400
    // =========================================================================

    @Test
    @DisplayName("TC08 [参数] 邮箱格式非法 — 返回 HTTP 400 校验错误")
    void tc08_login_invalidEmailFormat_returns400() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("email", "not-an-email", "password", "somePassword"));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    // =========================================================================
    // TC09  [参数校验]  密码为空 → 400
    // =========================================================================

    @Test
    @DisplayName("TC09 [参数] 密码为空 — 返回 HTTP 400 校验错误")
    void tc09_login_blankPassword_returns400() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("email", "valid@example.com", "password", ""));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    // =========================================================================
    // TC10  [参数校验]  请求体字段全部缺失 → 400
    // =========================================================================

    @Test
    @DisplayName("TC10 [参数] 请求体字段全部缺失 — 返回 HTTP 400")
    void tc10_login_emptyRequestBody_returns400() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
