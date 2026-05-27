package com.campushub.repository;

import com.campushub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 用户数据访问层接口.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /** 按邮箱查询用户（用于登录验证）. */
    Optional<User> findByEmail(String email);

    /** 检查邮箱是否已注册（用于注册去重）. */
    boolean existsByEmail(String email);

    /**
     * 动态调整用户信用分.
     * delta 为正数则加分，为负数则扣分。
     * 使用原生 SQL 原子操作，避免先查再写的并发问题。
     *
     * @param userId 被评价的用户 ID
     * @param delta  信用分变化量（正加负减）
     */
    @Modifying
    @Query(value = "UPDATE users SET credit_score = credit_score + :delta WHERE id = :userId",
           nativeQuery = true)
    int updateCreditScore(@Param("userId") Long userId, @Param("delta") BigDecimal delta);
}
