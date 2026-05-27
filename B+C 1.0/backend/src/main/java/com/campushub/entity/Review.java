package com.campushub.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 评价实体类，映射数据库 {@code reviews} 表.
 * <p>
 * 订单完成后，需求方对服务方进行评价。
 * 一个订单最多一条评价（uk_order_id 唯一约束保证）。
 * 评价提交后触发信用分更新。
 * </p>
 */
@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联订单，唯一约束确保每个订单只有一条评价 */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    /** 评分，1-5 星，数据库 CHECK 约束保证范围 */
    @Column(nullable = false)
    private Integer score;

    /** 评价文字内容，可选，最长 200 字符 */
    @Column(length = 200)
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
