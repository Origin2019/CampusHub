package com.example.backend.service;

import com.example.backend.mapper.OrderMapper;
import com.example.backend.mapper.TaskMapper;
import com.example.backend.model.entity.Order;
import com.example.backend.model.entity.Task;
import com.example.backend.model.enums.OrderStatus;
import com.example.backend.model.enums.TaskStatus;
import com.example.backend.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceAcceptTest {

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
    @DisplayName("越权拦截：不能承接自己发布的互助需求")
    void testAcceptTask_SelfPublish_ThrowException() {
        Long taskId = 100L;
        Long userId = 1L; // 既是发布者，又是接单者

        Task mockTask = new Task();
        mockTask.setId(taskId);
        mockTask.setRequesterId(userId);
        mockTask.setStatus(TaskStatus.published);

        when(taskMapper.selectById(taskId)).thenReturn(mockTask);

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            orderService.acceptTask(taskId, userId);
        });

        assertTrue(exception.getMessage().contains("您不能承接自己发布的互助需求"));
        verify(orderMapper, never()).insertOrder(any());
    }

    @Test
    @DisplayName("并发控制：高并发抢单手慢导致受影响行数为0，应提示抢单失败")
    void testAcceptTask_ConcurrentConflict_ThrowException() {
        Long taskId = 100L;
        Long requesterId = 1L;
        Long providerId = 2L;

        Task mockTask = new Task();
        mockTask.setId(taskId);
        mockTask.setRequesterId(requesterId);
        mockTask.setStatus(TaskStatus.published);

        when(taskMapper.selectById(taskId)).thenReturn(mockTask);
        // 模拟另一线程抢先一步更新，此线程原子更新返回 0
        when(taskMapper.updateTaskStatusForAccepting(taskId)).thenReturn(0);

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            orderService.acceptTask(taskId, providerId);
        });

        assertTrue(exception.getMessage().contains("手慢了"));
        verify(orderMapper, never()).insertOrder(any());
    }

    @Test
    @DisplayName("成功接单：状态匹配且抢到锁，生成新订单")
    void testAcceptTask_Success() {
        Long taskId = 100L;
        Long requesterId = 1L;
        Long providerId = 2L;

        Task mockTask = new Task();
        mockTask.setId(taskId);
        mockTask.setRequesterId(requesterId);
        mockTask.setStatus(TaskStatus.published);

        when(taskMapper.selectById(taskId)).thenReturn(mockTask);
        when(taskMapper.updateTaskStatusForAccepting(taskId)).thenReturn(1); // 成功锁定

        Order resultOrder = orderService.acceptTask(taskId, providerId);

        assertNotNull(resultOrder);
        assertEquals(OrderStatus.accepted, resultOrder.getStatus());
        assertEquals(providerId, resultOrder.getProviderId());
        verify(orderMapper, times(1)).insertOrder(any(Order.class));
    }
}