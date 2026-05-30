package com.campushub.task;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 发布需求的请求体 DTO.
 */
@Data
public class TaskCreateRequest {

    @NotBlank(message = "标题不能为空")
    @Size(min = 5, max = 50, message = "标题长度需在5-50字符之间")
    private String title;

    @NotBlank(message = "描述不能为空")
    @Size(min = 10, max = 500, message = "描述长度需在10-500字符之间")
    private String description;

    @NotNull(message = "分类ID不能为空")
    private Integer categoryId;

    @DecimalMin(value = "0.01", message = "报酬必须大于0")
    @DecimalMax(value = "99999999.99", message = "报酬超出合理范围")
    private BigDecimal reward;

    private String location;

    @NotNull(message = "截止时间不能为空")
    @Future(message = "截止时间必须晚于当前时间")
    private LocalDateTime deadline;
}
