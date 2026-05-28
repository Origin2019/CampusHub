package com.example.backend.service;

public interface ReviewService {
    /**
     * 提交订单评价
     * @param orderId 订单ID
     * @param currentUserId 当前登录用户ID
     * @param rating 评分（1-5）
     * @param comment 评论内容
     */
    void submitReview(Long orderId, Long currentUserId, Integer rating, String comment);
}