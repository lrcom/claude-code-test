package com.example.auth.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.auth.domain.model.User;
import com.example.auth.domain.repository.UserRepository;
import com.example.auth.infrastructure.persistence.mapper.UserMapper;
import com.example.auth.infrastructure.persistence.po.UserPO;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户仓储实现（领域 UserRepository 接口 → MyBatis-Plus）
 * <p>
 * 负责领域对象与 PO 之间的双向转换。
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;

    public UserRepositoryImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public void save(User user) {
        UserPO po = toUserPO(user);
        if (userMapper.selectById(po.getId()) != null) {
            userMapper.updateById(po);
        } else {
            userMapper.insert(po);
        }
    }

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(userMapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        UserPO po = userMapper.selectOne(
                new LambdaQueryWrapper<UserPO>().eq(UserPO::getEmail, email));
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        UserPO po = userMapper.selectOne(
                new LambdaQueryWrapper<UserPO>().eq(UserPO::getUsername, username));
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userMapper.selectCount(
                new LambdaQueryWrapper<UserPO>().eq(UserPO::getEmail, email)) > 0;
    }

    @Override
    public boolean existsByUsername(String username) {
        return userMapper.selectCount(
                new LambdaQueryWrapper<UserPO>().eq(UserPO::getUsername, username)) > 0;
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
