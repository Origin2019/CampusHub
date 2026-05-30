package com.campushub.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理后台需求列表响应 DTO（含发布者详情）.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskManagementDTO {
    private Long id;
    private String title;
    private String description;
    private String categoryName;
    private BigDecimal reward;
    private String status;
    private String location;
    private LocalDateTime deadline;
    private LocalDateTime publishedAt;
    /** 发布者 ID */
    private Long requesterId;
    /** 发布者姓名 */
    private String requesterName;
    /** 发布者邮箱 */
    private String requesterEmail;
}
