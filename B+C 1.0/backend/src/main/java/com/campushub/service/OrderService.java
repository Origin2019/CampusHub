package com.campushub.service;

import com.campushub.dto.OrderDTO;
import com.campushub.entity.Order;
import com.campushub.entity.Task;
import com.campushub.entity.User;
import com.campushub.exception.BusinessException;
import com.campushub.repository.OrderRepository;
import com.campushub.repository.TaskRepository;
import com.campushub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 订单业务逻辑层.
 * <p>
 * 负责接单、确认完成、取消订单三个核心流程。
 * 接单使用原子 SQL 防止高并发时多人同时抢单成功。
 * 取消订单根据操作方不同，触发不同的需求状态回滚策略：
 * <ul>
 *   <li>服务方取消 → 需求重新回到大厅（published）</li>
 *   <li>需求方取消 → 需求废弃（cancelled）</li>
 * </ul>
 * </p>
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    /**
     * 接单 — 服务方抢单核心流程.
     *
     * <ol>
     *   <li>校验需求存在 & 状态为 published</li>
     *   <li>越权校验：不能接自己发布的需求</li>
     *   <li>原子更新需求状态为 in_progress（防并发）</li>
     *   <li>创建订单记录</li>
     * </ol>
     */
    @Transactional
    public OrderDTO acceptTask(Long taskId, Long userId) {
        // 1. 需求存在性校验（使用 JOIN FETCH 预加载 requester，避免后续懒加载）
        Task task = taskRepository.findByIdWithDetails(taskId)
                .orElseThrow(() -> new BusinessException(404, "需求不存在"));

        // 2. 越权拦截：不能接自己发布的需求
        if (task.getRequester().getId().equals(userId)) {
            throw new BusinessException(403, "不能接自己发布的需求");
        }

        // 3. 状态预检（快速失败，减少无效的原子 SQL 调用）
        if (task.getStatus() != Task.TaskStatus.published) {
            throw new BusinessException(409, "该需求已被接单或已关闭");
        }

        // 4. 原子 SQL 抢单：利用数据库条件更新防止并发冲突
        //    另一线程已抢先更新状态时，affected rows = 0，当前线程抢单失败
        int updated = taskRepository.updateStatusForAccepting(taskId);
        if (updated == 0) {
            throw new BusinessException(409, "手慢了！该需求刚刚已被他人接单");
        }

        // 5. 查询服务方用户信息
        User provider = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        // 6. 创建订单
        Order order = Order.builder()
                .task(task)
                .requester(task.getRequester())
                .provider(provider)
                .status(Order.OrderStatus.accepted)
                .build();

        order = orderRepository.save(order);
        return toDTO(order);
    }

    /**
     * 确认完成订单 — 仅需求方可操作.
     */
    @Transactional
    public void completeOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));

        // 越权校验：只有需求方才能点"确认完成"
        if (!order.getRequester().getId().equals(userId)) {
            throw new BusinessException(403, "只有需求方才能确认完成订单");
        }

        if (order.getStatus() != Order.OrderStatus.accepted) {
            throw new BusinessException(409, "当前订单状态不可执行完成操作");
        }

        // 原子更新，防止并发重复操作
        int updated = orderRepository.completeOrder(orderId);
        if (updated == 0) {
            throw new BusinessException(409, "订单状态已改变，操作失败");
        }

        // 同步更新关联需求状态
        taskRepository.updateStatus(order.getTask().getId(), "completed");
    }

    /**
     * 取消订单 — 需求方或服务方均可操作，但后续逻辑不同.
     */
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));

        boolean isRequester = order.getRequester().getId().equals(userId);
        boolean isProvider  = order.getProvider().getId().equals(userId);

        if (!isRequester && !isProvider) {
            throw new BusinessException(403, "无权操作此订单");
        }

        if (order.getStatus() != Order.OrderStatus.accepted) {
            throw new BusinessException(409, "当前订单状态不可取消");
        }

        int updated = orderRepository.cancelOrder(orderId);
        if (updated == 0) {
            throw new BusinessException(409, "订单状态已改变，操作失败");
        }

        if (isProvider) {
            // 服务方反悔取消 → 需求重新释放回大厅，让别人接
            taskRepository.updateStatus(order.getTask().getId(), "published");
        } else {
            // 需求方主动取消 → 整个任务废弃
            taskRepository.updateStatus(order.getTask().getId(), "cancelled");
        }
    }

    /**
     * 查看订单详情 — 仅需求方和服务方可见.
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrderDetail(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));

        boolean isParticipant = order.getRequester().getId().equals(userId)
                || order.getProvider().getId().equals(userId);
        if (!isParticipant) {
            throw new BusinessException(403, "无权查看该订单");
        }

        return toDTO(order);
    }

    /** Order 实体 → OrderDTO 转换 */
    private OrderDTO toDTO(Order order) {
        Task task = order.getTask();
        User requester = order.getRequester();
        User provider  = order.getProvider();

        return OrderDTO.builder()
                .id(order.getId())
                .taskId(task.getId())
                .taskTitle(task.getTitle())
                .taskReward(task.getReward())
                .requesterId(requester.getId())
                .requesterName(requester.getName())
                .requesterAvatar(requester.getAvatar())
                .providerId(provider.getId())
                .providerName(provider.getName())
                .providerAvatar(provider.getAvatar())
                .status(order.getStatus().name())
                .acceptedAt(order.getAcceptedAt())
                .completedAt(order.getCompletedAt())
                .cancelledAt(order.getCancelledAt())
                .build();
    }
}
