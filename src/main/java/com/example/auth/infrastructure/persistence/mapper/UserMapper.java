package com.example.auth.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.infrastructure.persistence.po.UserPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 MyBatis-Plus Mapper
 * <p>
 * 继承 BaseMapper 获得完整的 CRUD 能力，无需手写 SQL。
 */
@Mapper
public interface UserMapper extends BaseMapper<UserPO> {
}
