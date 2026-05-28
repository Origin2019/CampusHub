package com.example.backend.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Review {
    private Long id;
    private Long orderId;
    private Long reviewerId;   // 评价人ID
    private Long revieweeId;   // 被评价人ID
    private String reviewerRole; // 评价人角色：'requester' 或 'provider'
    private Integer rating;    // 评分：1-5星
    private String comment;    // 评论内容
    private LocalDateTime createdAt;
}