package com.example.backend.mapper;

import com.example.backend.model.entity.Review;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ReviewMapper {

    /**
     * 插入一条评价记录
     */
    @Insert("INSERT INTO reviews (order_id, reviewer_id, reviewee_id, reviewer_role, rating, comment, created_at) " +
            "VALUES (#{orderId}, #{reviewerId}, #{revieweeId}, #{reviewerRole}, #{rating}, #{comment}, NOW())")
    int insertReview(Review review);

    /**
     * 检查某个用户是否已经对该订单评价过（用于防重复评价）
     */
    @Select("SELECT COUNT(*) FROM reviews WHERE order_id = #{orderId} AND reviewer_id = #{reviewerId}")
    int countByOrderIdAndReviewerId(@Param("orderId") Long orderId, @Param("reviewerId") Long reviewerId);

    /**
     * 统计某个订单的总评价数（上限为2条：需求方1条 + 服务方1条）
     */
    @Select("SELECT COUNT(*) FROM reviews WHERE order_id = #{orderId}")
    int countByOrderId(@Param("orderId") Long orderId);
}