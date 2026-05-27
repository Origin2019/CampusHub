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

    /**
     * 查询指定用户的接单履约历史（连表查询以获取任务详情）
     * 支持按订单状态筛选（如：进行中、已完结、已取消）
     */
    @Select("<script>" +
            "SELECT o.id AS orderId, o.status AS orderStatus, o.accepted_at, " +
            "       t.id AS taskId, t.title, t.description, t.reward, t.requester_id " +
            "FROM orders o " +
            "INNER JOIN tasks t ON o.task_id = t.id " +
            "WHERE o.provider_id = #{userId} " +
            "  <if test='status != null'> AND o.status = #{status} </if> " +
            "ORDER BY o.accepted_at DESC" +
            "</script>")
    java.util.List<java.util.Map<String, Object>> selectAcceptHistory(@Param("userId") Long userId, @Param("status") String status);
}