package com.example.auth.application.command;

/**
 * 注册命令
 */
public record RegisterCommand(String username, String email, String password) {
}
