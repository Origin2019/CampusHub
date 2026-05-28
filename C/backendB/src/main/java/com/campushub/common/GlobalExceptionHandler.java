package com.campushub.common;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常拦截处理器.
 * <p>
 * 使用 Spring {@code @RestControllerAdvice} 拦截所有 Controller 抛出的异常，
 * 将其统一转换为 {@link ApiResponse} 格式的 JSON 响应，
 * 避免异常细节直接暴露给前端。
 * </p>
 *
 * <h3>处理流程</h3>
 * <ol>
 *   <li>Controller → Service 抛出异常</li>
 *   <li>本类中匹配的 {@code @ExceptionHandler} 方法捕获</li>
 *   <li>根据异常类型和错误码构造 {@code ApiResponse}</li>
 *   <li>返回对应 HTTP 状态码的 ResponseEntity</li>
 * </ol>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常.
     * <p>
     * 捕获 Service 层抛出的 {@link BusinessException}，
     * 根据错误码映射为合适的 HTTP 状态码（401/403/404/409/429/400），
     * 错误信息直接展示给用户。
     * </p>
     *
     * @param ex Service 层抛出的业务异常
     * @return 包含错误码和消息的 ApiResponse，HTTP 状态码由错误码决定
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        HttpStatus httpStatus = resolveHttpStatus(ex.getCode());
        return ResponseEntity.status(httpStatus)
                .body(ApiResponse.error(ex.getCode(), ex.getMessage(), null));
    }

    /**
     * 处理 Jakarta Bean Validation 校验失败异常.
     * <p>
     * 当 Controller 参数标注了 {@code @Valid} 且校验不通过时，
     * Spring 抛出 {@link MethodArgumentNotValidException}。
     * 本方法收集所有字段校验失败的详细信息，拼接后放入 error 字段返回给前端。
     * </p>
     * <p>
     * 示例输出：{@code "title: 标题长度需在5-50字符之间; deadline: 截止时间必须晚于当前时间"}
     * </p>
     *
     * @param ex Spring 校验框架抛出的异常，包含各字段的校验失败信息
     * @return HTTP 422，error 字段为分号分隔的校验失败明细
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(422, "参数校验失败", detail));
    }

    /**
     * 兜底异常处理器.
     * <p>
     * 捕获所有未被上述 Handler 匹配的异常（如空指针、数据库连接失败等），
     * 统一返回 HTTP 500，避免异常堆栈直接泄露给客户端。
     * error 字段中包含异常消息供开发者排查。
     * </p>
     *
     * @param ex 未被其他 Handler 捕获的异常
     * @return HTTP 500，error 字段为异常消息原文
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "服务器内部错误", ex.getMessage()));
    }

    /**
     * 将业务错误码映射为 Spring HTTP 状态码.
     * <p>
     * 映射规则遵循 P3 API 规范文档的错误码定义：
     * 401→UNAUTHORIZED, 403→FORBIDDEN, 404→NOT_FOUND,
     * 409→CONFLICT, 429→TOO_MANY_REQUESTS, 其他→BAD_REQUEST.
     * </p>
     *
     * @param code {@link BusinessException} 中携带的业务错误码
     * @return 对应的 Spring {@link HttpStatus} 枚举值
     */
    private HttpStatus resolveHttpStatus(int code) {
        return switch (code) {
            case 401 -> HttpStatus.UNAUTHORIZED;
            case 403 -> HttpStatus.FORBIDDEN;
            case 404 -> HttpStatus.NOT_FOUND;
            case 409 -> HttpStatus.CONFLICT;
            case 429 -> HttpStatus.TOO_MANY_REQUESTS;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
