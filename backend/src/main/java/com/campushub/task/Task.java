package com.campushub.task;

import com.campushub.category.Category;
import com.campushub.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 需求（Task）实体类，映射数据库 {@code tasks} 表 — 需求发布板块的核心.
 * <p>
 * 一个需求由需求方（{@code requester} 角色）发布，归属于某个分类，
 * 具有发布、进行中、完成、取消四种状态。
 * 服务方接单后，需求状态变为 in_progress，同时创建 Order 记录。
 * </p>
 *
 * <h3>关联关系</h3>
 * <ul>
 *   <li>{@code @ManyToOne → User(requester)} — 每个需求属于一个发布者</li>
 *   <li>{@code @ManyToOne → Category} — 每个需求属于一个分类</li>
 *   <li>与 Order 是 1:N 关系，在 Order 实体中通过 FK 关联</li>
 * </ul>
 *
 * <h3>状态流转</h3>
 * <pre>
 *   published ──(接单)──→ in_progress ──(完成)──→ completed
 *       │                                              │
 *       └──────────── (取消) ←─────────────────────────┘
 * </pre>
 */
@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    /** 需求唯一标识，数据库自增主键 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 发布者（需求方）.
     * 使用 LAZY 延迟加载，仅在需要时通过 SQL 查询 User 信息，
     * 避免每次查询 Task 都附带查询 User。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    /**
     * 所属分类.
     * 使用 LAZY 延迟加载，关联 categories 表的外键 category_id。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /** 需求标题，最长 50 字符，用于列表展示和关键词搜索 */
    @Column(nullable = false, length = 50)
    private String title;

    /** 需求详细描述，最长 500 字符 */
    @Column(nullable = false, length = 500)
    private String description;

    /** 报酬金额，DECIMAL(10,2)，可为 null 表示免费/面议 */
    @Column(precision = 10, scale = 2)
    private BigDecimal reward;

    /**
     * 需求当前状态.
     * <p>
     * 使用 {@code @Enumerated(EnumType.STRING)} 以字符串形式存储到数据库 ENUM 字段，
     * 而非枚举的序数（ordinal），防止枚举值顺序变动导致数据错乱。
     * 默认状态为 published。
     * </p>
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('published','in_progress','completed','cancelled')")
    @Builder.Default
    private TaskStatus status = TaskStatus.published;

    /** 服务地点，如"3号宿舍楼下"，最长 100 字符，可选 */
    @Column(length = 100)
    private String location;

    /** 截止时间（Deadline），超过此时间需求自动视为过期，可选 */
    private LocalDateTime deadline;

    /** 发布时间，默认为当前时间，用于首页倒序排列 */
    @Column(name = "published_at", nullable = false)
    @Builder.Default
    private LocalDateTime publishedAt = LocalDateTime.now();

    /** 记录创建时间（物理），不可更新 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 记录更新时间（物理），每次 UPDATE 自动刷新 */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 需求状态枚举 — 控制需求在业务流程中的生命周期.
     * <p>
     * 合法流转路径由接单模块（OrderService）的状态机校验保证。
     * </p>
     */
    public enum TaskStatus {
        /** 已发布，等待服务方接单 */
        published,
        /** 进行中，已有服务方接单正在执行 */
        in_progress,
        /** 已完成，服务方标记完成 */
        completed,
        /** 已取消，需求方或服务方主动取消 */
        cancelled
    }

    /**
     * JPA 持久化前回调.
     * 确保首次保存时 publishedAt、createdAt、updatedAt 三个时间戳都被正确设置。
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (publishedAt == null) publishedAt = now;
        createdAt = now;
        updatedAt = now;
    }

    /**
     * JPA 更新前回调，刷新 updatedAt 时间戳.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
