package com.example.auth.application.service;

import com.example.auth.application.command.LoginCommand;
import com.example.auth.application.command.RegisterCommand;
import com.example.auth.application.dto.LoginResult;
import com.example.auth.application.dto.UserInfo;
import com.example.auth.application.port.TokenService;
import com.example.auth.domain.event.DomainEvent;
import com.example.auth.domain.model.User;
import com.example.auth.domain.repository.UserRepository;
import com.example.auth.domain.service.PasswordEncoder;
import com.example.auth.domain.service.UserDomainService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 认证应用服务
 * <p>
 * 职责：编排领域对象完成用例，不包含业务规则。
 * 事务边界在此层控制。
 */
@Service
@Transactional
public class AuthApplicationService {

    private final UserRepository userRepository;
    private final UserDomainService userDomainService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final ApplicationEventPublisher eventPublisher;

    public AuthApplicationService(UserRepository userRepository,
                                  UserDomainService userDomainService,
                                  PasswordEncoder passwordEncoder,
                                  TokenService tokenService,
                                  ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.userDomainService = userDomainService;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 注册用例
     */
    public void register(RegisterCommand command) {
        // 领域校验：唯一性规则由领域服务保证
        userDomainService.validateUniqueEmail(command.email());
        userDomainService.validateUniqueUsername(command.username());

        // 创建聚合（业务规则封装在聚合内）
        User user = User.register(command.username(), command.email(),
                command.password(), passwordEncoder);

        // 持久化
        userRepository.save(user);

        // 发布领域事件
        publishEvents(user.pullDomainEvents());
    }

    /**
     * 登录用例
     */
    public LoginResult login(LoginCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + command.email()));

        // 业务行为委托给聚合根
        user.login(command.password(), passwordEncoder);

        // 持久化（更新 updatedAt）
        userRepository.save(user);

        // 生成 Token
        String token = tokenService.generateToken(user.getId().getValue(), user.getUsername());

        // 发布领域事件
        publishEvents(user.pullDomainEvents());

        return new LoginResult(token, user.getId().getValue(),
                user.getUsername(), user.getEmail().getValue());
    }

    private void publishEvents(List<DomainEvent> events) {
        events.forEach(eventPublisher::publishEvent);
    }

    /**
     * 查询用户信息
     */
    @Transactional(readOnly = true)
    public UserInfo getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return new UserInfo(user.getId().getValue(), user.getUsername(),
                user.getEmail().getValue(), user.getCreatedAt());
    }
}
