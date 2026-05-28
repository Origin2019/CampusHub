package com.example.backend.model.entity;

import com.example.backend.model.enums.TaskStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Task {
    private Long id;
    private Long requesterId;
    private Integer categoryId;
    private String title;
    private String description;
    private BigDecimal reward;
    private TaskStatus status; // 使用枚举映射数据库 ENUM
    private String location;
    private LocalDateTime deadline;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}