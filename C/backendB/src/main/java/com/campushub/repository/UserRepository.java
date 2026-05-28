package com.campushub.repository;

import com.campushub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 用户数据访问层接口.
 * <p>
 * 继承 {@link JpaRepository} 获得基础 CRUD 能力，
 * 额外定义按邮箱查询的方法供认证和注册模块使用。
 * </p>
 * <p>
 * 方法命名遵循 Spring Data JPA 命名约定：
 * {@code findBy + 字段名} 会自动生成对应的 SELECT 查询，
 * 无需手写 JPQL。
 * </p>
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 按邮箱地址精确查询用户.
     * 用于登录时的"邮箱+密码"校验。
     *
     * @param email 用户注册时使用的学校邮箱
     * @return 匹配该邮箱的用户 Optional，若不存在则为 empty
     */
    Optional<User> findByEmail(String email);

    /**
     * 检查指定邮箱是否已被注册.
     * 用于注册时检测邮箱唯一性。
     *
     * @param email 待检查的学校邮箱
     * @return true=邮箱已被占用，false=邮箱可用
     */
    boolean existsByEmail(String email);
}
