package com.campushub.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 订单实体类，映射数据库 {@code orders} 表.
 * <p>
 * 服务方接单时创建订单，记录需求方与服务方之间的交易过程。
 * 状态流转：accepted → completed / cancelled
 * </p>
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联的互助需求 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    /** 需求方（发布需求的用户） */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    /** 服务方（接单的用户） */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('accepted','in_progress','completed','cancelled')")
    @Builder.Default
    private OrderStatus status = OrderStatus.accepted;

    @Column(name = "accepted_at", nullable = false)
    @Builder.Default
    private LocalDateTime acceptedAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 订单状态枚举.
     * accepted=已接单, in_progress=进行中, completed=已完成, cancelled=已取消
     */
    public enum OrderStatus {
        accepted, in_progress, completed, cancelled
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (acceptedAt == null) acceptedAt = now;
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
