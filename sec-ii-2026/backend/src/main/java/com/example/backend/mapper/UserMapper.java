package com.example.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import java.math.BigDecimal;

@Mapper
public interface UserMapper {

    /**
     * 动态加减用户信用分
     * @param userId 用户ID
     * @param delta 变化的积分值（可以是正数或负数）
     */
    @Update("UPDATE users SET credit_score = credit_score + #{delta} WHERE id = #{userId}")
    int updateCreditScore(@Param("userId") Long userId, @Param("delta") BigDecimal delta);
}