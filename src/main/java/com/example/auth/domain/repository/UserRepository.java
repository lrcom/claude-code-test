package com.example.auth.domain.repository;

import com.example.auth.domain.model.User;

import java.util.Optional;

/**
 * 用户仓储接口（领域层定义，基础设施层实现）
 */
public interface UserRepository {

    void save(User user);

    Optional<User> findById(String id);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
