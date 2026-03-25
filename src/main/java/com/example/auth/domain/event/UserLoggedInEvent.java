package com.example.auth.domain.event;

/**
 * 用户登录领域事件
 */
public class UserLoggedInEvent extends DomainEvent {

    private final String userId;
    private final String username;

    public UserLoggedInEvent(String userId, String username) {
        super();
        this.userId = userId;
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
}
