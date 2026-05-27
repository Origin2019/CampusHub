package com.example.backend.model.entity;

import com.example.backend.model.enums.OrderStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Order {
    private Long id;
    private Long taskId;
    private Long requesterId;
    private Long providerId;
    private OrderStatus status; // 使用枚举映射数据库 ENUM
    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}