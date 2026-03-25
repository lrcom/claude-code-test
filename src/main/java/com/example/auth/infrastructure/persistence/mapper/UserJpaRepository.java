package com.example.auth.infrastructure.persistence.mapper;

import com.example.auth.infrastructure.persistence.po.UserPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA 仓储接口
 */
public interface UserJpaRepository extends JpaRepository<UserPO, String> {

    Optional<UserPO> findByEmail(String email);

    Optional<UserPO> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
