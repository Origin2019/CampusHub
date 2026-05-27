package com.campushub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单响应 DTO.
 * 将 Order 实体展开为前端可直接使用的扁平结构，不暴露敏感信息。
 */
@Data
@Builder
@AllArgsConstructor
public class OrderDTO {

    private Long id;

    /** 关联需求简要信息 */
    private Long taskId;
    private String taskTitle;
    private BigDecimal taskReward;

    /** 需求方简要信息 */
    private Long requesterId;
    private String requesterName;
    private String requesterAvatar;

    /** 服务方简要信息 */
    private Long providerId;
    private String providerName;
    private String providerAvatar;

    /** 订单状态：accepted / in_progress / completed / cancelled */
    private String status;

    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
}
