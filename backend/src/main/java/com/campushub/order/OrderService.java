package com.campushub.order;

import com.campushub.common.BusinessException;
import com.campushub.order.Order.OrderStatus;
import com.campushub.task.Task;
import com.campushub.task.Task.TaskStatus;
import com.campushub.task.TaskRepository;
import com.campushub.user.User;
import com.campushub.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public OrderDTO acceptTask(Long taskId, Long currentUserId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(404, "互助需求不存在"));
        User provider = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        if (task.getRequester().getId().equals(currentUserId)) {
            throw new BusinessException(403, "不能承接自己发布的互助需求");
        }
        if (provider.getRole() != User.Role.provider) {
            throw new BusinessException(403, "仅服务方可以接单");
        }
        if (task.getStatus() != TaskStatus.published) {
            throw new BusinessException(400, "该需求已被承接或已关闭");
        }

        int affectedRows = taskRepository.updateStatusIfCurrent(taskId, TaskStatus.published, TaskStatus.in_progress);
        if (affectedRows == 0) {
            throw new BusinessException(409, "该需求刚刚已被其他用户接单");
        }

        task.setStatus(TaskStatus.in_progress);

        Order order = new Order();
        order.setTask(task);
        order.setRequester(task.getRequester());
        order.setProvider(provider);
        order.setStatus(OrderStatus.accepted);

        return toDTO(orderRepository.save(order));
    }

    @Transactional
    public void completeOrder(Long orderId, Long currentUserId) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));

        if (!order.getRequester().getId().equals(currentUserId)) {
            throw new BusinessException(403, "只有需求方可以确认完成订单");
        }
        if (order.getStatus() != OrderStatus.accepted && order.getStatus() != OrderStatus.in_progress) {
            throw new BusinessException(400, "当前订单状态无法确认完成");
        }

        order.setStatus(OrderStatus.completed);
        order.setCompletedAt(LocalDateTime.now());
        order.getTask().setStatus(TaskStatus.completed);
    }

    @Transactional
    public void cancelOrder(Long orderId, Long currentUserId) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));

        boolean isRequester = order.getRequester().getId().equals(currentUserId);
        boolean isProvider = order.getProvider().getId().equals(currentUserId);
        if (!isRequester && !isProvider) {
            throw new BusinessException(403, "无权操作此订单");
        }
        if (order.getStatus() != OrderStatus.accepted && order.getStatus() != OrderStatus.in_progress) {
            throw new BusinessException(400, "当前订单状态无法取消");
        }

        order.setStatus(OrderStatus.cancelled);
        order.setCancelledAt(LocalDateTime.now());
        order.getTask().setStatus(isProvider ? TaskStatus.published : TaskStatus.cancelled);
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getAcceptHistory(Long userId, String status) {
        OrderStatus orderStatus = parseStatus(status);
        return orderRepository.findAcceptHistory(userId, orderStatus).stream()
                .map(this::toDTO)
                .toList();
    }

    private OrderStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return OrderStatus.valueOf(status);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(400, "订单状态参数不合法");
        }
    }

    private OrderDTO toDTO(Order order) {
        Task task = order.getTask();
        return OrderDTO.builder()
                .orderId(order.getId())
                .orderStatus(order.getStatus().name())
                .acceptedAt(order.getAcceptedAt())
                .completedAt(order.getCompletedAt())
                .cancelledAt(order.getCancelledAt())
                .taskId(task.getId())
                .taskStatus(task.getStatus().name())
                .title(task.getTitle())
                .description(task.getDescription())
                .reward(task.getReward())
                .requesterId(order.getRequester().getId())
                .providerId(order.getProvider().getId())
                .build();
    }
}
