package com.campushub.review;

import com.campushub.common.BusinessException;
import com.campushub.order.Order;
import com.campushub.order.Order.OrderStatus;
import com.campushub.order.OrderRepository;
import com.campushub.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public void submitReview(Long orderId, Long currentUserId, Integer score, String comment) {
        if (score == null || score < 1 || score > 5) {
            throw new BusinessException(400, "评分必须在 1 到 5 之间");
        }

        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));
        if (!order.getRequester().getId().equals(currentUserId)) {
            throw new BusinessException(403, "只有需求方可以评价订单");
        }
        if (order.getStatus() != OrderStatus.completed) {
            throw new BusinessException(400, "只有已完成的订单可以评价");
        }
        if (Boolean.FALSE.equals(order.getTask().getCategory().getHasReview())) {
            throw new BusinessException(400, "该分类不支持评价");
        }
        if (reviewRepository.existsByOrder_Id(orderId)) {
            throw new BusinessException(409, "该订单已评价，请勿重复提交");
        }

        Review review = new Review();
        review.setOrder(order);
        review.setScore(score);
        review.setComment(comment);
        reviewRepository.save(review);

        applyCreditScore(order.getProvider(), score);
    }

    private void applyCreditScore(User reviewee, Integer score) {
        int delta = 0;
        if (score == 5) {
            delta = 1;
        } else if (score <= 2) {
            delta = -2;
        }
        if (delta != 0) {
            int currentScore = reviewee.getCreditScore() == null ? 0 : reviewee.getCreditScore();
            reviewee.setCreditScore(currentScore + delta);
        }
    }
}
