package com.example.auth.infrastructure.persistence;

import com.example.auth.domain.model.User;
import com.example.auth.domain.repository.UserRepository;
import com.example.auth.infrastructure.persistence.mapper.UserJpaRepository;
import com.example.auth.infrastructure.persistence.po.UserPO;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户仓储实现（领域 UserRepository 接口 → Spring Data JPA）
 * <p>
 * 负责领域对象与 PO 之间的双向转换。
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;

    public UserRepositoryImpl(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(User user) {
        jpaRepository.save(toUserPO(user));
    }

    @Override
    public Optional<User> findById(String id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    // -----------------------------------------------------------------------
    // 转换
    // -----------------------------------------------------------------------

    private UserPO toUserPO(User user) {
        UserPO po = new UserPO();
        po.setId(user.getId().getValue());
        po.setUsername(user.getUsername());
        po.setEmail(user.getEmail().getValue());
        po.setPassword(user.getPassword().getEncodedValue());
        po.setStatus(user.getStatus());
        po.setCreatedAt(user.getCreatedAt());
        po.setUpdatedAt(user.getUpdatedAt());
        return po;
    }

    private User toDomain(UserPO po) {
        return User.reconstitute(
                po.getId(),
                po.getUsername(),
                po.getEmail(),
                po.getPassword(),
                po.getStatus(),
                po.getCreatedAt(),
                po.getUpdatedAt()
        );
    }
}
