package com.campushub.controller;

import com.campushub.common.ApiResponse;
import com.campushub.common.PageResult;
import com.campushub.dto.TaskCreateRequest;
import com.campushub.dto.TaskDTO;
import com.campushub.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 需求（Task）REST 控制器 — 需求发布板块的入口层.
 * <p>
 * 所有接口的前缀为 {@code /v1/tasks}，遵循 P3 API 规范文档定义。
 * 控制器仅负责：
 * <ul>
 *   <li>接收 HTTP 请求并提取参数</li>
 *   <li>调用 Service 层方法</li>
 *   <li>将结果包装为 {@link ApiResponse} 统一格式返回</li>
 * </ul>
 * 不包含任何业务逻辑，业务逻辑全部在 {@link TaskService} 中实现。
 * </p>
 *
 * <h3>认证说明</h3>
 * 发布需求接口需要登录认证（仅 requester 角色），
 * 当前临时使用 {@code X-User-Id} 请求头传递用户 ID，
 * 后续集成 JWT + Spring Security 后替换为从 SecurityContext 提取当前用户。
 *
 * @see TaskService
 */
@RestController
@RequestMapping("/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    /** 需求业务逻辑层 */
    private final TaskService taskService;

    /**
     * 发布一条新的互助需求.
     * <p>
     * 对应 P3 API 规范中的 {@code POST /demands} 接口。
     * 请求体由 {@link TaskCreateRequest} 承载，通过 {@code @Valid} 自动触发 Jakarta Bean Validation 校验。
     * 校验规则（定义在 TaskCreateRequest 各字段注解上）：
     * <ul>
     *   <li>标题：5-50 字符</li>
     *   <li>描述：10-500 字符</li>
     *   <li>分类ID：必填且必须为合法分类</li>
     *   <li>截止时间：必填且必须晚于当前时间</li>
     * </ul>
     * </p>
     * <p>
     * TODO: JWT 认证实现后，从 SecurityContext 获取 userId，替换 X-User-Id 临时方案.
     * </p>
     *
     * @param request 前端表单数据，经 @Valid 校验
     * @param userId  临时用户标识（X-User-Id 请求头），后续改为 JWT 提取
     * @return HTTP 201，data 字段为保存成功的需求详情
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TaskDTO> createTask(
            @Valid @RequestBody TaskCreateRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        TaskDTO task = taskService.createTask(request, userId);
        return ApiResponse.success(task);
    }

    /**
     * 查询需求列表（分页 + 多条件筛选）.
     * <p>
     * 对应 P3 API 规范中的 {@code GET /demands} 接口，无需登录即可访问。
     * 支持以下查询参数的自由组合：
     * <ul>
     *   <li>{@code categoryId} — 按分类 ID 筛选</li>
     *   <li>{@code status} — 按需求状态筛选（published/in_progress/completed/cancelled）</li>
     *   <li>{@code keyword} — 按标题关键词模糊搜索</li>
     *   <li>{@code sort} — 排序方式（latest=最新优先，reward=报酬降序），默认 latest</li>
     *   <li>{@code page} — 页码，从 1 开始，默认 1</li>
     *   <li>{@code page_size} — 每页条数，默认 20</li>
     * </ul>
     * </p>
     *
     * @param categoryId 分类 ID（可选）
     * @param status     需求状态（可选）
     * @param keyword    搜索关键词（可选）
     * @param sort       排序方式（默认 "latest"）
     * @param page       页码（默认 1）
     * @param size       每页条数（默认 20）
     * @return 分页结果，items 为当前页的需求列表，pagination 为分页元信息
     */
    @GetMapping
    public ApiResponse<PageResult<TaskDTO>> listTasks(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(name = "page_size", defaultValue = "20") int size) {
        PageResult<TaskDTO> result = taskService.listTasks(categoryId, status, keyword, sort, page, size);
        return ApiResponse.success(result);
    }

    /**
     * 查询需求详情.
     * <p>
     * 通过路径参数 {@code id} 定位需求，
     * 返回内容包含需求全部信息和发布者简要信息（昵称、头像），
     * 不需要登录即可查看。
     * </p>
     *
     * @param id 需求主键 ID
     * @return 需求详情，包含发布者信息
     */
    @GetMapping("/{id}")
    public ApiResponse<TaskDTO> getTaskDetail(@PathVariable Long id) {
        TaskDTO task = taskService.getTaskDetail(id);
        return ApiResponse.success(task);
    }
}
