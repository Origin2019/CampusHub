package com.campushub.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 提交评价的请求体 DTO.
 */
@Data
public class ReviewRequest {

    /** 评分，1-5 星，必填 */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低 1 星")
    @Max(value = 5, message = "评分最高 5 星")
    private Integer score;

    /** 评价内容，可选，最长 200 字符 */
    @Size(max = 200, message = "评价内容不能超过 200 字符")
    private String comment;
}
