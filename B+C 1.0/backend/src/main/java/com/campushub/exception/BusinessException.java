package com.campushub.exception;

import lombok.Getter;

/**
 * 业务异常类.
 * <p>
 * 当业务规则被违反时（如角色不对、分类不存在、重复接单等），
 * Service 层抛出此异常，由 {@link GlobalExceptionHandler} 统一拦截
 * 并转换为符合 API 规范的错误响应 JSON。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>
 * if (requester.getRole() != Role.requester) {
 *     throw new BusinessException(403, "仅需求方可以发布需求");
 * }
 * // 不传 code 时默认为 400
 * throw new BusinessException("参数不合法");
 * </pre>
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 业务错误码.
     * 会被 {@link GlobalExceptionHandler#resolveHttpStatus} 映射为 HTTP 状态码：
     * 401→UNAUTHORIZED, 403→FORBIDDEN, 404→NOT_FOUND, 409→CONFLICT,
     * 429→TOO_MANY_REQUESTS, 其余→BAD_REQUEST(400)
     */
    private final int code;

    /**
     * 构造带自定义错误码的业务异常.
     *
     * @param code    业务错误码，用于映射 HTTP 状态码
     * @param message 面向用户的错误描述信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造默认错误码（400）的业务异常.
     *
     * @param message 面向用户的错误描述信息
     */
    public BusinessException(String message) {
        this(400, message);
    }
}
