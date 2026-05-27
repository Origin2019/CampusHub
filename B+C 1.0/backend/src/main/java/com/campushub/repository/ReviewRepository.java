package com.campushub.repository;

import com.campushub.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 评价数据访问层.
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 检查某订单是否已存在评价（用于防止重复评价）.
     */
    @Query("SELECT COUNT(r) > 0 FROM Review r WHERE r.order.id = :orderId")
    boolean existsByOrderId(@Param("orderId") Long orderId);
}
