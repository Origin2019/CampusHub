package com.campushub.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 需求响应 DTO — 用于列表和详情接口的统一返回格式.
 */
@Data
@Builder
@AllArgsConstructor
public class TaskDTO {

    private Long id;
    private String title;
    private String description;
    private String categoryName;
    private Integer categoryId;
    private BigDecimal reward;
    private String status;
    private String location;
    private LocalDateTime deadline;
    private LocalDateTime publishedAt;
    private Publisher publisher;

    @Data
    @Builder
    @AllArgsConstructor
    public static class Publisher {
        private Long userId;
        private String nickname;
        private String avatar;
    }
}
