package com.campushub.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 发布需求的请求体 DTO.
 * <p>
 * 前端发布需求表单提交的 JSON 数据由此类接收。
 * 每个字段上的 Jakarta Bean Validation 注解会在 Controller 层
 * 由 {@code @Valid} 触发校验，校验失败时抛出
 * {@link org.springframework.web.bind.MethodArgumentNotValidException}，
 * 由 {@link com.campushub.exception.GlobalExceptionHandler} 统一拦截。
 * </p>
 */
@Data
public class TaskCreateRequest {

    /**
     * 需求标题.
     * 校验规则：不为空，长度 1-50 个字符。
     */
    @NotBlank(message = "标题不能为空")
    @Size(min = 1, max = 50, message = "标题长度需在1-50字符之间")
    private String title;

    /**
     * 需求详细描述.
     * 校验规则：不为空，长度 1-500 个字符。
     */
    @NotBlank(message = "描述不能为空")
    @Size(min = 1, max = 500, message = "描述长度需在1-500字符之间")
    private String description;

    /**
     * 分类 ID.
     * 必须为 categories 表中存在的合法 ID，
     * Service 层会进一步校验分类是否存在。
     */
    @NotNull(message = "分类ID不能为空")
    private Integer categoryId;

    /**
     * 报酬金额.
     * 可选字段，若填写则必须大于 0 且在合理范围内（0.01–99999999.99）。
     * 不填写表示"免费"或"面议"。
     */
    @DecimalMin(value = "0.01", message = "报酬必须大于0")
    @DecimalMax(value = "99999999.99", message = "报酬超出合理范围")
    private BigDecimal reward;

    /**
     * 服务地点，如"图书馆一楼"，可选.
     */
    private String location;

    /**
     * 截止时间.
     * 必须晚于当前时间（使用 {@code @Future} 校验），
     * 超过截止时间的需求不应再被接单。
     */
    @NotNull(message = "截止时间不能为空")
    @Future(message = "截止时间必须晚于当前时间")
    private LocalDateTime deadline;
}
