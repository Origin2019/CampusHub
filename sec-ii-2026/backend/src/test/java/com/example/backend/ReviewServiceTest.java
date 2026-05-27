package com.example.backend.service;

import com.example.backend.mapper.OrderMapper;
import com.example.backend.mapper.ReviewMapper;
import com.example.backend.mapper.TaskMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.model.entity.Order;
import com.example.backend.model.enums.OrderStatus;
import com.example.backend.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewServiceTest {

    private OrderMapper orderMapper;
    private ReviewMapper reviewMapper;
    private UserMapper userMapper;
    private TaskMapper taskMapper;
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        orderMapper = mock(OrderMapper.class);
        reviewMapper = mock(ReviewMapper.class);
        userMapper = mock(UserMapper.class);
        taskMapper = mock(TaskMapper.class);
        reviewService = new ReviewServiceImpl(orderMapper, reviewMapper, userMapper, taskMapper);
    }

    @Test
    @DisplayName("信用分计算：5星好评给对方加1分，1星差评扣2分")
    void testSubmitReview_CreditScoreCalculation() {
        Long orderId = 300L;
        Long requesterId = 1L;
        Long providerId = 2L;

        Order mockOrder = new Order();
        mockOrder.setId(orderId);
        mockOrder.setRequesterId(requesterId);
        mockOrder.setProviderId(providerId);
        mockOrder.setStatus(OrderStatus.completed);

        when(orderMapper.selectById(orderId)).thenReturn(mockOrder);
        when(reviewMapper.countByOrderIdAndReviewerId(orderId, requesterId)).thenReturn(0);
        // 假设当前是第1条评价，不会触发完结闭环
        when(reviewMapper.countByOrderId(orderId)).thenReturn(1);

        // 1. 需求方给服务方打了 5 星
        reviewService.submitReview(orderId, requesterId, 5, "太棒了！");
        // 应该给服务方(providerId = 2) 加 1.0 分
        verify(userMapper, times(1)).updateCreditScore(providerId, new BigDecimal("1.0"));

        // 2. 模拟另一案：需求方给服务方打 1 星
        reviewService.submitReview(orderId, requesterId, 1, "态度恶劣");
        // 应该给服务方扣 2.0 分
        verify(userMapper, times(1)).updateCreditScore(providerId, new BigDecimal("-2.0"));
    }

    @Test
    @DisplayName("防重复评价：应用层已评价过的，直接拦截抛出异常")
    void testSubmitReview_DuplicateReview_ThrowException() {
        Long orderId = 300L;
        Long requesterId = 1L;

        Order mockOrder = new Order();
        mockOrder.setId(orderId);
        mockOrder.setRequesterId(requesterId);
        mockOrder.setStatus(OrderStatus.completed);

        when(orderMapper.selectById(orderId)).thenReturn(mockOrder);
        // 模拟应用层查到该用户已经对该订单发表过评价
        when(reviewMapper.countByOrderIdAndReviewerId(orderId, requesterId)).thenReturn(1);

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            reviewService.submitReview(orderId, requesterId, 5, "好评");
        });

        assertTrue(exception.getMessage().contains("请勿重复提交"));
        verify(reviewMapper, never()).insertReview(any());
    }

    @Test
    @DisplayName("核心闭环：单方评价订单不完结，双方互评后订单及任务触发终态流转")
    void testSubmitReview_DualReview_TriggersClosure() {
        Long orderId = 300L;
        Long taskId = 700L;
        Long requesterId = 1L;
        Long providerId = 2L;

        Order mockOrder = new Order();
        mockOrder.setId(orderId);
        mockOrder.setTaskId(taskId);
        mockOrder.setRequesterId(requesterId);
        mockOrder.setProviderId(providerId);
        mockOrder.setStatus(OrderStatus.completed);

        when(orderMapper.selectById(orderId)).thenReturn(mockOrder);
        when(reviewMapper.countByOrderIdAndReviewerId(orderId, requesterId)).thenReturn(0);

        // 关键点：模拟买卖双方都已经提交了记录（总条数增加到2）
        when(reviewMapper.countByOrderId(orderId)).thenReturn(2);
        when(orderMapper.updateOrderToEvaluated(orderId)).thenReturn(1);

        // 执行评价逻辑
        reviewService.submitReview(orderId, requesterId, 5, "合作愉快");

        // 校验是否正确锁定了订单和任务的最终状态状态
        verify(orderMapper, times(1)).updateOrderToEvaluated(orderId);
        verify(taskMapper, times(1)).updateTaskStatus(taskId, "evaluated");
    }
}