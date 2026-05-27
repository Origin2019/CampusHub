package com.campushub.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 统一 API 响应包装类.
 * <p>
 * 所有 Controller 的返回值都通过此类包装，
 * 确保前端收到的 JSON 拥有统一的结构。
 * </p>
 *
 * <h3>响应格式示例</h3>
 * <pre>
 * // 成功 — 带数据
 * { "code": 200, "message": "success", "data": { ... } }
 *
 * // 失败 — 带错误详情
 * { "code": 400, "message": "参数错误", "error": "title: 标题不能为空" }
 * </pre>
 *
 * @param <T> data 字段的泛型类型，可以是单个对象、列表、分页结果等
 */
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)  // 序列化 JSON 时跳过 null 字段，保持响应体简洁
public class ApiResponse<T> {

    /** HTTP 业务状态码：200=成功，400=参数错误，401=未认证，403=无权限，404=未找到，409=冲突，422=校验失败，500=服务端错误 */
    private int code;

    /** 简要状态描述，成功时为 "success"，失败时为错误摘要 */
    private String message;

    /** 成功时的业务数据载体 */
    private T data;

    /** 失败时的详细错误信息，仅在 code != 200 时有值 */
    private String error;

    /**
     * 构建成功响应（带数据体）.
     *
     * @param data 业务数据，可以是任意类型
     * @param <T>  数据类型
     * @return code=200, message="success" 的响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data, null);
    }

    /**
     * 构建成功响应（无数据体，用于仅需告知操作成功的情况，如删除）.
     *
     * @param <T> 数据类型（Void）
     * @return code=200, message="success" 且 data 为 null 的响应
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "success", null, null);
    }

    /**
     * 构建失败响应.
     *
     * @param code    业务错误码，会被映射为对应的 HTTP 状态码
     * @param message 面向用户的错误描述
     * @param error   面向开发者的详细错误信息（如校验字段列表、堆栈摘要）
     * @param <T>     数据类型（通常为 Void）
     * @return 包含错误信息的响应体
     */
    public static <T> ApiResponse<T> error(int code, String message, String error) {
        return new ApiResponse<>(code, message, null, error);
    }
}
