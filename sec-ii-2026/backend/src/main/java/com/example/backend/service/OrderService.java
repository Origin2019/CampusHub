package com.example.backend.service;

import com.example.backend.model.entity.Order;

public interface OrderService {
    /**
     * 接单核心接口
     * @param taskId 互助需求ID
     * @param currentUserId 当前登录抢单的用户ID（服务方）
     * @return 成功生成的订单信息
     */
    Order acceptTask(Long taskId, Long currentUserId);

    /**
     * 确认完成订单（由需求方操作）
     * @param orderId 订单ID
     * @param currentUserId 当前操作用户ID
     */
    void completeOrder(Long orderId, Long currentUserId);

    /**
     * 取消订单（关闭交易）
     * @param orderId 订单ID
     * @param currentUserId 当前操作用户ID
     */
    void cancelOrder(Long orderId, Long currentUserId);
}