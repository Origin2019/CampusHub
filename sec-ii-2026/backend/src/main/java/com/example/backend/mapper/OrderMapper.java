package com.example.backend.mapper;

import com.example.backend.model.entity.Order;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OrderMapper {

    /**
     * 插入新生成的订单记录
     */
    @Insert("INSERT INTO orders (task_id, requester_id, provider_id, status, accepted_at, created_at, updated_at) " +
            "VALUES (#{taskId}, #{requesterId}, #{providerId}, 'accepted', NOW(), NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertOrder(Order order);

    /**
     * 根据订单ID查询详情
     */
    @Select("SELECT * FROM orders WHERE id = #{id}")
    Order selectById(@Param("id") Long id);

    /**
     * 更新订单状态为完成，并记录完成时间
     */
    @Update("UPDATE orders SET status = 'completed', completed_at = NOW(), updated_at = NOW() " +
            "WHERE id = #{orderId} AND status = 'accepted'")
    int updateOrderToCompleted(@Param("orderId") Long orderId);

    /**
     * 更新订单状态为取消，并记录取消时间
     */
    @Update("UPDATE orders SET status = 'cancelled', cancelled_at = NOW(), updated_at = NOW() " +
            "WHERE id = #{orderId} AND status = 'accepted'")
    int updateOrderToCancelled(@Param("orderId") Long orderId);

    /**
     * 更新订单状态为已评价完结
     */
    @Update("UPDATE orders SET status = 'evaluated', updated_at = NOW() " +
            "WHERE id = #{orderId} AND status = 'completed'")
    int updateOrderToEvaluated(@Param("orderId") Long orderId);
}
