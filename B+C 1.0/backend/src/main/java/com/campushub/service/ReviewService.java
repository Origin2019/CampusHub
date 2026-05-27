package com.campushub.service;

import com.campushub.entity.Order;
import com.campushub.entity.Review;
import com.campushub.exception.BusinessException;
import com.campushub.repository.OrderRepository;
import com.campushub.repository.ReviewRepository;
import com.campushub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 评价业务逻辑层.
 * <p>
 * 仅需求方可以对服务方进行评价（订单完成后）。
 * 评价后根据星级更新服务方信用分：
 * <ul>
 *   <li>5 星 → +1.0 分</li>
 *   <li>4 星 → +0.5 分</li>
 *   <li>3 星 → 不变</li>
 *   <li>1-2 星 → -2.0 分</li>
 * </ul>
 * </p>
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    /**
     * 提交评价.
     *
     * @param orderId 被评价的订单 ID
     * @param userId  当前操作用户 ID（必须是需求方）
     * @param score   评分 1-5
     * @param comment 评价内容（可选）
     */
    @Transactional
    public void submitReview(Long orderId, Long userId, Integer score, String comment) {
        // 1. 查询订单
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));

        // 2. 状态校验：只有已完成的订单才能评价
        if (order.getStatus() != Order.OrderStatus.completed) {
            throw new BusinessException(409, "只有已完成的订单才能评价");
        }

        // 3. 权限校验：只有需求方可以评价（评价服务方）
        if (!order.getRequester().getId().equals(userId)) {
            throw new BusinessException(403, "只有需求方才能评价该订单");
        }

        // 4. 防重复评价
        if (reviewRepository.existsByOrderId(orderId)) {
            throw new BusinessException(409, "该订单已被评价");
        }

        // 5. 保存评价记录
        Review review = Review.builder()
                .order(order)
                .score(score)
                .comment(comment)
                .build();
        reviewRepository.save(review);

        // 6. 信用分联动：根据星级更新服务方信用分
        BigDecimal delta = calcCreditDelta(score);
        if (delta.compareTo(BigDecimal.ZERO) != 0) {
            userRepository.updateCreditScore(order.getProvider().getId(), delta);
        }
    }

    /**
     * 根据评分计算信用分变化量.
     */
    private BigDecimal calcCreditDelta(int score) {
        return switch (score) {
            case 5 -> new BigDecimal("1.0");
            case 4 -> new BigDecimal("0.5");
            case 1, 2 -> new BigDecimal("-2.0");
            default -> BigDecimal.ZERO;  // 3 星不变
        };
    }
}
