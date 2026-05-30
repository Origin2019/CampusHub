package com.campushub.task;

import com.campushub.common.BusinessException;
import com.campushub.common.PageResult;
import com.campushub.common.PageResult.Pagination;
import com.campushub.category.Category;
import com.campushub.category.CategoryRepository;
import com.campushub.user.User;
import com.campushub.user.User.Role;
import com.campushub.user.UserRepository;
import com.campushub.task.Task.TaskStatus;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 需求（Task）业务逻辑层.
 * <p>
 * 负责需求发布板块的全部业务规则：
 * <ul>
 *   <li>发布需求的角色校验与数据持久化</li>
 *   <li>需求列表的动态多条件筛选与分页查询</li>
 *   <li>需求详情的查询（含关联发布者信息）</li>
 * </ul>
 * </p>
 * <p>
 * 所有方法都标注了 Spring 的 {@code @Transactional}，确保数据库操作在一个事务中完成。
 * 读方法使用 {@code readOnly = true} 优化数据库连接资源。
 * </p>
 */
@Service
@RequiredArgsConstructor  // Lombok：自动生成包含所有 final 字段的构造器，用于依赖注入
public class TaskService {

    /** 需求数据访问 */
    private final TaskRepository taskRepository;

    /** 分类数据访问 */
    private final CategoryRepository categoryRepository;

    /** 用户数据访问 */
    private final UserRepository userRepository;

    /**
     * 发布一条新的互助需求.
     * <p>
     * 执行流程（按顺序）：
     * <ol>
     *   <li>根据 userId 查询用户，用户不存在则抛出 404</li>
     *   <li>校验用户角色：只有 requester（需求方）才能发布，否则抛出 403</li>
     *   <li>校验分类 ID 是否存在于 categories 表，不存在抛出 404</li>
     *   <li>将请求参数转换为 Task 实体并保存到数据库</li>
     *   <li>将保存后的 Task 转为 TaskDTO 返回给 Controller</li>
     * </ol>
     * </p>
     * <p>
     * {@code @Transactional} 确保以上操作在一个数据库事务中完成，
     * 任何一步失败都会回滚所有已执行的 SQL。
     * </p>
     *
     * @param request 前端提交的发布需求表单数据，已经过 @Valid 客户端校验
     * @param userId  当前登录用户的 ID（临时通过 X-User-Id Header 传入，后续替换为 JWT）
     * @return 保存成功后的需求 DTO，包含生成的 id 和发布时间
     * @throws BusinessException 用户不存在（404）/ 非需求方角色（403）/ 分类不存在（404）
     */
    @Transactional
    public TaskDTO createTask(TaskCreateRequest request, Long userId) {
        // 1. 校验发布者存在性
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        // 2. 校验角色：只有需求方可发布
        if (requester.getRole() != Role.requester) {
            throw new BusinessException(403, "仅需求方可以发布需求");
        }

        // 3. 校验分类合法性
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(404, "分类不存在"));

        // 4. 构建实体并保存
        Task task = Task.builder()
                .requester(requester)
                .category(category)
                .title(request.getTitle())
                .description(request.getDescription())
                .reward(request.getReward())
                .location(request.getLocation())
                .deadline(request.getDeadline())
                .status(TaskStatus.published)  // 新发布的需求状态固定为 published
                .build();

        task = taskRepository.save(task);
        return toDTO(task);
    }

    /**
     * 分页查询需求列表（支持多条件动态筛选）.
     * <p>
     * 使用 Spring Data JPA 的 {@link Specification} 机制动态构建 WHERE 条件：
     * 根据传入参数是否为 null 来决定是否添加对应的筛选条件，
     * 避免为每种参数组合编写单独的 JPQL 查询。
     * </p>
     * <p>
     * 筛选逻辑（所有条件之间为 AND 关系）：
     * <ul>
     *   <li>categoryId 非 null → WHERE category_id = categoryId</li>
     *   <li>status 非空 → WHERE status = status（非法 status 值会被静默忽略）</li>
     *   <li>keyword 非空 → WHERE title LIKE '%keyword%'</li>
     * </ul>
     * 排列：默认按 published_at 降序（最新在前），sort=reward 时按报酬降序。
     * </p>
     *
     * @param categoryId 分类 ID 筛选，null 表示不筛选
     * @param status     需求状态筛选，null 或空串表示不筛选
     * @param keyword    标题关键词模糊搜索，null 或空串表示不筛选
     * @param sortBy     排序字段："latest"（默认，按发布时间倒序）/ "reward"（按报酬倒序）
     * @param page       页码，从 1 开始，默认 1
     * @param size       每页条数，默认 20
     * @return 分页结果，包含当前页数据列表和分页元信息
     */
    @Transactional(readOnly = true)
    public PageResult<TaskDTO> listTasks(Integer categoryId, String status,
                                          String keyword, String sortBy,
                                          int page, int size) {
        // 构建动态查询条件
        Specification<Task> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 按分类 ID 精确匹配
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            // 按需求状态精确匹配（非法字符串静默跳过）
            if (status != null && !status.isBlank()) {
                try {
                    TaskStatus ts = TaskStatus.valueOf(status);
                    predicates.add(cb.equal(root.get("status"), ts));
                } catch (IllegalArgumentException ignored) {
                    // 非法的 status 参数忽略
                }
            }
            // 按标题模糊搜索
            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(root.get("title"), "%" + keyword + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 确定排序方向 + 分页参数
        Sort sort = buildSort(sortBy);
        Pageable pageable = PageRequest.of(page - 1, size, sort);  // Spring Data 页码从 0 开始，API 从 1 开始

        // 执行分页查询
        Page<Task> taskPage = taskRepository.findAll(spec, pageable);

        // 将每一条 Task 实体转换为 TaskDTO
        List<TaskDTO> items = taskPage.getContent().stream()
                .map(this::toDTO)
                .toList();

        // 构建分页元信息
        Pagination pagination = new Pagination(
                page, size, taskPage.getTotalElements(), taskPage.getTotalPages()
        );

        return new PageResult<>(items, pagination);
    }

    /**
     * 查询需求详情（含发布者信息和分类信息）.
     * <p>
     * 使用自定义 JPQL {@code JOIN FETCH} 一次性加载 Task + User + Category，
     * 避免在 toDTO 转换时触发懒加载造成的 N+1 查询问题。
     * </p>
     *
     * @param id 需求主键 ID
     * @return 需求详情 DTO，包含发布者昵称、头像、分类名称等
     * @throws BusinessException 需求不存在（404）
     */
    @Transactional(readOnly = true)
    public TaskDTO getTaskDetail(Long id) {
        Task task = taskRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new BusinessException(404, "需求不存在"));
        return toDTO(task);
    }

    /**
     * 根据排序参数构建 Spring Data 的 Sort 对象.
     * <p>
     * 当前支持的排序方式：
     * <ul>
     *   <li>"reward" — 按报酬金额降序</li>
     *   <li>其他 / 默认 — 按发布时间降序（最新需求在前）</li>
     * </ul>
     * </p>
     *
     * @param sortBy 前端传入的排序参数
     * @return Spring Data Sort 对象
     */
    private Sort buildSort(String sortBy) {
        if ("reward".equals(sortBy)) {
            return Sort.by(Sort.Direction.DESC, "reward");
        }
        return Sort.by(Sort.Direction.DESC, "publishedAt");
    }

    /**
     * 将 Task 实体转换为 TaskDTO 响应对象.
     * <p>
     * 该方法是核心的数据转换函数，负责：
     * <ul>
     *   <li>从 Task 懒加载关联中提取 User 的展示字段（id, name, avatar）</li>
     *   <li>从 Category 关联中提取分类名称</li>
     *   <li>将枚举 status 转为字符串</li>
     * </ul>
     * 注意：此方法必须在事务上下文中调用（即被 @Transactional 方法调用），
     * 因为需要访问 LAZY 加载的 requester 和 category 关联。
     * </p>
     *
     * @param task 数据库中的 Task 实体对象
     * @return 前端可用的 TaskDTO，不会包含任何敏感信息
     */
    private TaskDTO toDTO(Task task) {
        User requester = task.getRequester();
        Category category = task.getCategory();

        return TaskDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                // 处理边界：虽然 category 正常情况不会为 null，但防御性处理
                .categoryName(category != null ? category.getName() : null)
                .categoryId(category != null ? category.getId() : null)
                .reward(task.getReward())
                .status(task.getStatus().name())
                .location(task.getLocation())
                .deadline(task.getDeadline())
                .publishedAt(task.getPublishedAt())
                // 构建发布者信息子对象
                .publisher(TaskDTO.Publisher.builder()
                        .userId(requester.getId())
                        .nickname(requester.getName())
                        .avatar(requester.getAvatar())
                        .build())
                .build();
    }
}
