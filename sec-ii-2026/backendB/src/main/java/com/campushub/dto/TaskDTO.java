package com.campushub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 需求响应 DTO — 用于列表和详情接口的统一返回格式.
 * <p>
 * 与数据库实体 {@link com.campushub.entity.Task} 不同，
 * 此 DTO 将发布者和分类的外键 ID 展开为具体信息：
 * <ul>
 *   <li>categoryId → categoryId + categoryName</li>
 *   <li>requester → Publisher(用户ID + 昵称 + 头像)</li>
 * </ul>
 * 避免前端拿到 DTO 后还需要二次请求后端获取发布者名称。
 * </p>
 */
@Data
@Builder
@AllArgsConstructor
public class TaskDTO {

    /** 需求唯一标识 */
    private Long id;

    /** 需求标题 */
    private String title;

    /** 需求详细描述 */
    private String description;

    /** 分类中文名称（如"快递代取"），直接可用于前端展示 */
    private String categoryName;

    /** 分类 ID，供前端筛选/跳转使用 */
    private Integer categoryId;

    /** 报酬金额，null 表示免费/面议 */
    private BigDecimal reward;

    /** 需求当前状态：published / in_progress / completed / cancelled */
    private String status;

    /** 服务地点 */
    private String location;

    /** 截止时间 */
    private LocalDateTime deadline;

    /** 发布时间 */
    private LocalDateTime publishedAt;

    /** 发布者简要信息 */
    private Publisher publisher;

    /**
     * 发布者简要信息内部类.
     * <p>
     * 仅包含前端列表和详情页需要展示的发布者字段，
     * 不包含密码、手机号等敏感信息。
     * </p>
     */
    @Data
    @Builder
    @AllArgsConstructor
    public static class Publisher {

        /** 发布者用户 ID */
        private Long userId;

        /** 发布者昵称（真实姓名） */
        private String nickname;

        /** 发布者头像 URL */
        private String avatar;
    }
}
