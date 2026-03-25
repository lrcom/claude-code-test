package com.example.auth.domain.event;

/**
 * 用户注册领域事件
 */
public class UserRegisteredEvent extends DomainEvent {

    private final String userId;
    private final String username;
    private final String email;

    public UserRegisteredEvent(String userId, String username, String email) {
        super();
        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}
