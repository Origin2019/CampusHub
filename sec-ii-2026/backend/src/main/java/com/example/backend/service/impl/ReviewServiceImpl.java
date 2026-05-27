package com.example.backend.service.impl;

import com.example.backend.mapper.OrderMapper;
import com.example.backend.mapper.ReviewMapper;
import com.example.backend.mapper.TaskMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.model.entity.Order;
import com.example.backend.model.entity.Review;
import com.example.backend.model.enums.OrderStatus;
import com.example.backend.service.ReviewService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final OrderMapper orderMapper;
    private final ReviewMapper reviewMapper;
    private final UserMapper userMapper;
    private final TaskMapper taskMapper; // 注入 TaskMapper 用于流转关联需求状态

    public ReviewServiceImpl(OrderMapper orderMapper, ReviewMapper reviewMapper,
                             UserMapper userMapper, TaskMapper taskMapper) {
        this.orderMapper = orderMapper;
        this.reviewMapper = reviewMapper;
        this.userMapper = userMapper;
        this.taskMapper = taskMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitReview(Long orderId, Long currentUserId, Integer rating, String comment) {
        // 1. 验证评分合法性
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("评分参数错误，必须在 1 到 5 星之间！");
        }

        // 2. 验证订单状态：只有“已完成(completed)”或“已互评中”的订单才能评价（这里支持后续状态流转）
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("该订单不存在！");
        }
        if (order.getStatus() != OrderStatus.completed) {
            throw new IllegalStateException("评价失败：只有已确认完成的订单才能进行评价！");
        }

        // 3. 校验身份并确定被评价人ID及角色
        Long revieweeId;
        String role;
        if (order.getRequesterId().equals(currentUserId)) {
            role = "requester";                // 当前用户是需求方
            revieweeId = order.getProviderId(); // 被评价人是服务方
        } else if (order.getProviderId().equals(currentUserId)) {
            role = "provider";                // 当前用户是服务方
            revieweeId = order.getRequesterId(); // 被评价人是需求方
        } else {
            throw new IllegalStateException("越权拦截：您不是该订单的当事人，无权评价！");
        }

        // 4. 防重复评价校验（应用层先查）
        int hasReviewed = reviewMapper.countByOrderIdAndReviewerId(orderId, currentUserId);
        if (hasReviewed > 0) {
            throw new IllegalStateException("您已经评价过该订单，请勿重复提交！");
        }

        // 5. 保存评价记录（捕获数据库联合唯一索引异常，兜底高并发连击）
        Review review = new Review();
        review.setOrderId(orderId);
        review.setReviewerId(currentUserId);
        review.setRevieweeId(revieweeId);
        review.setReviewerRole(role);
        review.setRating(rating);
        review.setComment(comment);

        try {
            reviewMapper.insertReview(review);
        } catch (DuplicateKeyException e) {
            throw new IllegalStateException("请勿重复提交评价！");
        }

        // 6. 信用分计算联动规则
        BigDecimal deltaScore = BigDecimal.ZERO;
        if (rating == 5) {
            deltaScore = new BigDecimal("1.0");
        } else if (rating == 4) {
            deltaScore = new BigDecimal("0.5");
        } else if (rating == 1 || rating == 2) {
            deltaScore = new BigDecimal("-2.0");
        }

        if (deltaScore.compareTo(BigDecimal.ZERO) != 0) {
            userMapper.updateCreditScore(revieweeId, deltaScore);
        }

        // 7. 【新增核心闭环逻辑】双方互评检查与订单终态流转
        // 检查当前订单在评价表里一共有几条记录
        int totalReviewCount = reviewMapper.countByOrderId(orderId);
        if (totalReviewCount >= 2) {
            // 说明买卖双方都完成了评价，触发状态机向终态流转
            int affectedOrders = orderMapper.updateOrderToEvaluated(orderId);
            if (affectedOrders > 0) {
                // 同步将关联的 Task 状态修改为 evaluated 完结
                taskMapper.updateTaskStatus(order.getTaskId(), "evaluated");
            }
        }
    }
}