package com.campushub.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户实体类，映射数据库 {@code users} 表.
 * <p>
 * 用户是平台的核心参与者，支持三种角色：
 * <ul>
 *   <li>{@code requester} — 需求方，可以发布互助需求</li>
 *   <li>{@code provider} — 服务方，可以接单提供帮助</li>
 *   <li>{@code admin} — 管理员，拥有后台管理权限</li>
 * </ul>
 * </p>
 * <p>
 * 敏感字段处理：
 * <ul>
 *   <li>密码通过 bcrypt 加盐哈希后存入 {@code passwordHash}，永不明文存储</li>
 *   <li>手机号通过 AES-256 加密后存入 {@code phone}，API 返回时需脱敏</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /** 用户唯一标识，数据库自增主键 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 用户真实姓名，最长 20 字符 */
    @Column(nullable = false, length = 20)
    private String name;

    /** 学校邮箱，全局唯一，用于登录和身份验证 */
    @Column(nullable = false, length = 100, unique = true)
    private String email;

    /** 手机号，AES-256 加密存储，数据库唯一约束 */
    @Column(nullable = false, length = 255)
    private String phone;

    /** bcrypt 加盐哈希后的密码（cost factor = 10），永不明文存储 */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /** 用户角色枚举：requester（需求方）/ provider（服务方）/ admin（管理员） */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('requester','provider','admin')")
    private Role role;

    /** 信用评分，范围 1.0–5.0，默认 5.0，由评价完成后自动重算 */
    @Column(name = "credit_score", nullable = false, precision = 3, scale = 1)
    @Builder.Default
    private BigDecimal creditScore = new BigDecimal("5.0");

    /** 头像图片 URL，最长 500 字符 */
    @Column(length = 500)
    private String avatar;

    /** 账号状态：1=正常，0=禁用（由管理员操作） */
    @Column(nullable = false)
    @Builder.Default
    private Integer status = 1;

    /** 记录创建时间，由数据库自动填充，不可更新 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 记录更新时间，每次 UPDATE 由数据库自动刷新 */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** 用户角色枚举 */
    public enum Role {
        /** 需求方：发布需求 */
        requester,
        /** 服务方：接单提供帮助 */
        provider,
        /** 管理员：审核与管理 */
        admin
    }

    /**
     * JPA 持久化前自动调用的生命周期回调.
     * 在 INSERT 之前设置创建时间和更新时间。
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * JPA 更新前自动调用的生命周期回调.
     * 在 UPDATE 之前刷新更新时间。
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
