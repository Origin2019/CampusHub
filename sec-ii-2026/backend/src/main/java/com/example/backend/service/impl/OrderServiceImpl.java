package com.example.backend.service.impl;

import com.example.backend.mapper.OrderMapper;
import com.example.backend.mapper.TaskMapper;
import com.example.backend.model.entity.Order;
import com.example.backend.model.entity.Task;
import com.example.backend.model.enums.OrderStatus;
import com.example.backend.model.enums.TaskStatus;
import com.example.backend.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

    private final TaskMapper taskMapper;
    private final OrderMapper orderMapper;

    public OrderServiceImpl(TaskMapper taskMapper, OrderMapper orderMapper) {
        this.taskMapper = taskMapper;
        this.orderMapper = orderMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order acceptTask(Long taskId, Long currentUserId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("该互助需求不存在！");
        }

        if (task.getRequesterId().equals(currentUserId)) {
            throw new IllegalStateException("越权拦截：您不能承接自己发布的互助需求！");
        }

        if (task.getStatus() != TaskStatus.published) {
            throw new IllegalStateException("抢单失败：该需求已被他人承接或已关闭！");
        }

        int affectedRows = taskMapper.updateTaskStatusForAccepting(taskId);
        if (affectedRows == 0) {
            throw new IllegalStateException("手慢了！该需求刚刚已被其他同学抢先接单！");
        }

        Order newOrder = new Order();
        newOrder.setTaskId(taskId);
        newOrder.setRequesterId(task.getRequesterId());
        newOrder.setProviderId(currentUserId);
        newOrder.setStatus(OrderStatus.accepted);

        orderMapper.insertOrder(newOrder);
        return newOrder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Long orderId, Long currentUserId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("该订单不存在！");
        }

        // 越权校验：只有需求方（发布任务、出钱的人）才能点确认完成
        if (!order.getRequesterId().equals(currentUserId)) {
            throw new IllegalStateException("越权操作拦截：只有需求方才能确认完成该订单！");
        }

        // 状态机校验：只有进行中（accepted）的订单才能变更完成
        if (order.getStatus() != OrderStatus.accepted) {
            throw new IllegalStateException("操作失败：当前订单状态无法变更为完成状态！");
        }

        // 原子更新订单状态，防并发冲突
        int affectedOrders = orderMapper.updateOrderToCompleted(orderId);
        if (affectedOrders == 0) {
            throw new IllegalStateException("操作失败：订单状态已被锁定或已改变！");
        }

        // 同步修改关联的需求任务状态
        taskMapper.updateTaskStatus(order.getTaskId(), "completed");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, Long currentUserId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("该订单不存在！");
        }

        // 越权校验：只有订单的当事双方才有权取消
        boolean isRequester = order.getRequesterId().equals(currentUserId);
        boolean isProvider = order.getProviderId().equals(currentUserId);
        if (!isRequester && !isProvider) {
            throw new IllegalStateException("越权操作拦截：您无权操作此订单！");
        }

        if (order.getStatus() != OrderStatus.accepted) {
            throw new IllegalStateException("操作失败：当前订单状态无法取消！");
        }

        int affectedOrders = orderMapper.updateOrderToCancelled(orderId);
        if (affectedOrders == 0) {
            throw new IllegalStateException("操作失败：订单状态已发生改变！");
        }

        // 状态回滚机制
        if (isProvider) {
            // 服务方反悔：把需求释放回大厅，让别人继续接
            taskMapper.updateTaskStatus(order.getTaskId(), "published");
        } else {
            // 需求方取消：整个任务废弃
            taskMapper.updateTaskStatus(order.getTaskId(), "cancelled");
        }
    }
}