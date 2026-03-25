package com.example.auth.domain.service;

import com.example.auth.domain.exception.EmailAlreadyExistsException;
import com.example.auth.domain.exception.UsernameAlreadyExistsException;
import com.example.auth.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

/**
 * 用户领域服务
 * <p>
 * 处理跨聚合或需要查询仓储才能完成的业务规则
 */
@Service
public class UserDomainService {

    private final UserRepository userRepository;

    public UserDomainService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 校验邮箱唯一性
     */
    public void validateUniqueEmail(String email) {
        if (userRepository.existsByEmail(email.toLowerCase().trim())) {
            throw new EmailAlreadyExistsException(email);
        }
    }

    public void validateUniqueUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException(username);
        }
    }
}
