package com.campushub.repository;

import com.campushub.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 订单数据访问层.
 * <p>
 * 继承 JpaRepository 获得基础 CRUD，额外定义两个原子更新方法：
 * 使用 nativeQuery 直接操作数据库，利用 WHERE status=? 条件防止并发冲突。
 * </p>
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 原子更新订单状态为"已完成".
     * 只有当前状态为 accepted 时才执行，防止重复操作。
     */
    @Modifying
    @Query(value = "UPDATE orders SET status = 'completed', completed_at = NOW(), updated_at = NOW() " +
                   "WHERE id = :orderId AND status = 'accepted'", nativeQuery = true)
    int completeOrder(@Param("orderId") Long orderId);

    /**
     * 原子更新订单状态为"已取消".
     * 只有当前状态为 accepted 时才执行。
     */
    @Modifying
    @Query(value = "UPDATE orders SET status = 'cancelled', cancelled_at = NOW(), updated_at = NOW() " +
                   "WHERE id = :orderId AND status = 'accepted'", nativeQuery = true)
    int cancelOrder(@Param("orderId") Long orderId);
}
