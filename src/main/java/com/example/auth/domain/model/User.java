package com.example.auth.domain.model;

import com.example.auth.domain.event.DomainEvent;
import com.example.auth.domain.event.UserLoggedInEvent;
import com.example.auth.domain.event.UserRegisteredEvent;
import com.example.auth.domain.exception.AccountInactiveException;
import com.example.auth.domain.exception.AccountLockedException;
import com.example.auth.domain.exception.InvalidPasswordException;
import com.example.auth.domain.service.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 用户聚合根
 * <p>
 * 职责：封装用户注册、登录的业务规则，维护用户生命周期状态
 */
public class User {

    private final UserId id;
    private String username;
    private Email email;
    private Password password;
    private UserStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 待发布的领域事件 */
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // -----------------------------------------------------------------------
    // 构造（私有，通过工厂方法或重建方法创建）
    // -----------------------------------------------------------------------

    private User(UserId id, String username, Email email, Password password,
                 UserStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // -----------------------------------------------------------------------
    // 工厂方法 - 注册新用户
    // -----------------------------------------------------------------------

    /**
     * 注册新用户（工厂方法）
     *
     * @param username        用户名
     * @param rawEmail        邮箱（明文）
     * @param rawPassword     密码（明文）
     * @param passwordEncoder 密码编码器（领域服务接口）
     * @return 新建的用户聚合
     */
    public static User register(String username, String rawEmail,
                                String rawPassword, PasswordEncoder passwordEncoder) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be blank");
        }
        if (username.length() < 3 || username.length() > 32) {
            throw new IllegalArgumentException("Username must be between 3 and 32 characters");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        UserId id = UserId.generate();
        Email email = Email.of(rawEmail);
        Password password = Password.encoded(passwordEncoder.encode(rawPassword));
        LocalDateTime now = LocalDateTime.now();

        User user = new User(id, username, email, password, UserStatus.ACTIVE, now, now);
        user.domainEvents.add(new UserRegisteredEvent(id.getValue(), username, email.getValue()));
        return user;
    }

    // -----------------------------------------------------------------------
    // 重建方法 - 从持久化数据还原聚合（不触发领域事件）
    // -----------------------------------------------------------------------

    public static User reconstitute(String id, String username, String email,
                                    String encodedPassword, UserStatus status,
                                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new User(
                UserId.of(id),
                username,
                Email.of(email),
                Password.encoded(encodedPassword),
                status,
                createdAt,
                updatedAt
        );
    }

    // -----------------------------------------------------------------------
    // 业务行为
    // -----------------------------------------------------------------------

    /**
     * 登录校验
     *
     * @param rawPassword     输入的明文密码
     * @param passwordEncoder 密码编码器
     * @throws AccountLockedException   账号被锁定
     * @throws AccountInactiveException 账号未激活
     * @throws InvalidPasswordException 密码错误
     */
    public void login(String rawPassword, PasswordEncoder passwordEncoder) {
        if (status == UserStatus.LOCKED) {
            throw new AccountLockedException();
        }
        if (status == UserStatus.INACTIVE) {
            throw new AccountInactiveException();
        }
        if (!password.matches(rawPassword, passwordEncoder)) {
            throw new InvalidPasswordException();
        }
        this.updatedAt = LocalDateTime.now();
        domainEvents.add(new UserLoggedInEvent(id.getValue(), username));
    }

    /** 锁定账号 */
    public void lock() {
        if (status == UserStatus.LOCKED) {
            return;
        }
        this.status = UserStatus.LOCKED;
        this.updatedAt = LocalDateTime.now();
    }

    /** 激活账号 */
    public void activate() {
        this.status = UserStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    // -----------------------------------------------------------------------
    // 领域事件管理
    // -----------------------------------------------------------------------

    /**
     * 拉取并清空待发布的领域事件
     */
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return Collections.unmodifiableList(events);
    }

    // -----------------------------------------------------------------------
    // Getters
    // -----------------------------------------------------------------------

    public UserId getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Email getEmail() {
        return email;
    }

    public Password getPassword() {
        return password;
    }

    public UserStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
