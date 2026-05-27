package com.example.backend.service;

import com.example.backend.mapper.OrderMapper;
import com.example.backend.mapper.TaskMapper;
import com.example.backend.model.entity.Order;
import com.example.backend.model.enums.OrderStatus;
import com.example.backend.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class OrderServiceCancelTest {

    private TaskMapper taskMapper;
    private OrderMapper orderMapper;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        taskMapper = mock(TaskMapper.class);
        orderMapper = mock(OrderMapper.class);
        orderService = new OrderServiceImpl(taskMapper, orderMapper);
    }

    @Test
    @DisplayName("回滚机制：服务方反悔取消，需求应重新释放回大厅")
    void testCancelOrder_ByProvider_RollbackToPublished() {
        Long orderId = 200L;
        Long taskId = 500L;
        Long requesterId = 1L;
        Long providerId = 2L;

        Order mockOrder = new Order();
        mockOrder.setId(orderId);
        mockOrder.setTaskId(taskId);
        mockOrder.setRequesterId(requesterId);
        mockOrder.setProviderId(providerId);
        mockOrder.setStatus(OrderStatus.accepted);

        when(orderMapper.selectById(orderId)).thenReturn(mockOrder);
        when(orderMapper.updateOrderToCancelled(orderId)).thenReturn(1);

        // 服务方取消
        orderService.cancelOrder(orderId, providerId);

        verify(orderMapper, times(1)).updateOrderToCancelled(orderId);
        // 关键点：Task 应回到 published
        verify(taskMapper, times(1)).updateTaskStatus(taskId, "published");
    }

    @Test
    @DisplayName("废弃机制：需求方主动取消，任务流转为关闭状态")
    void testCancelOrder_ByRequester_TaskCancelled() {
        Long orderId = 200L;
        Long taskId = 500L;
        Long requesterId = 1L;
        Long providerId = 2L;

        Order mockOrder = new Order();
        mockOrder.setId(orderId);
        mockOrder.setTaskId(taskId);
        mockOrder.setRequesterId(requesterId);
        mockOrder.setProviderId(providerId);
        mockOrder.setStatus(OrderStatus.accepted);

        when(orderMapper.selectById(orderId)).thenReturn(mockOrder);
        when(orderMapper.updateOrderToCancelled(orderId)).thenReturn(1);

        // 需求方取消
        orderService.cancelOrder(orderId, requesterId);

        verify(orderMapper, times(1)).updateOrderToCancelled(orderId);
        // 关键点：Task 应被设为 cancelled
        verify(taskMapper, times(1)).updateTaskStatus(taskId, "cancelled");
    }
}