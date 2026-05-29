package com.campushub.admin;

import com.campushub.category.Category;
import com.campushub.category.CategoryRepository;
import com.campushub.common.BusinessException;
import com.campushub.common.PageResult;
import com.campushub.notification.Notification;
import com.campushub.notification.NotificationRepository;
import com.campushub.task.Task;
import com.campushub.task.TaskRepository;
import com.campushub.user.User;
import com.campushub.user.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final NotificationRepository notificationRepository;

    private User requireAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        if (user.getRole() != User.Role.admin) {
            throw new BusinessException(403, "无管理员权限");
        }
        return user;
    }

    // ==================== 仪表盘 ====================

    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats(Long adminUserId) {
        requireAdmin(adminUserId);

        long totalUsers = userRepository.count();
        long totalTasks = taskRepository.count();
        long totalNotifications = notificationRepository.count();
        long totalCategories = categoryRepository.count();

        long bannedUsers = userRepository.findAll().stream()
                .filter(u -> u.getStatus() != null && u.getStatus() != 1).count();

        List<Task> allTasks = taskRepository.findAll();
        long publishedTasks = allTasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.published).count();
        long inProgressTasks = allTasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.in_progress).count();
        long completedTasks = allTasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.completed).count();
        long cancelledTasks = allTasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.cancelled).count();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        long todayNewTasks = allTasks.stream()
                .filter(t -> t.getPublishedAt() != null && t.getPublishedAt().isAfter(todayStart)).count();

        return DashboardStatsDTO.builder()
                .totalUsers(totalUsers).bannedUsers(bannedUsers)
                .totalTasks(totalTasks).publishedTasks(publishedTasks)
                .inProgressTasks(inProgressTasks).completedTasks(completedTasks)
                .totalOrders(cancelledTasks).todayNewTasks(todayNewTasks)
                .build();
    }

    // ==================== 用户管理 ====================

    @Transactional(readOnly = true)
    public PageResult<UserManagementDTO> listUsers(Long adminUserId, int page, int size,
                                                    String keyword, String role) {
        requireAdmin(adminUserId);

        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isBlank()) {
                String kw = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), kw),
                        cb.like(cb.lower(root.get("email")), kw),
                        cb.like(cb.lower(root.get("studentId")), kw)
                ));
            }
            if (role != null && !role.isBlank()) {
                try {
                    predicates.add(cb.equal(root.get("role"), User.Role.valueOf(role)));
                } catch (IllegalArgumentException ignored) {}
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<User> userPage = userRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime")));

        List<UserManagementDTO> items = userPage.getContent().stream()
                .map(this::toUserDTO).collect(Collectors.toList());

        return new PageResult<>(items, userPage.getTotalElements(), page, size);
    }

    @Transactional(readOnly = true)
    public UserManagementDTO getUserDetail(Long adminUserId, Long targetId) {
        requireAdmin(adminUserId);
        User u = userRepository.findById(targetId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        return toUserDTO(u);
    }

    @Transactional
    public void toggleUserStatus(Long adminUserId, Long targetUserId) {
        requireAdmin(adminUserId);
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException(404, "目标用户不存在"));
        target.setStatus(target.getStatus() != null && target.getStatus() == 1 ? 0 : 1);
        userRepository.save(target);
    }

    // ==================== 需求管理 ====================

    @Transactional(readOnly = true)
    public PageResult<TaskManagementDTO> listAllTasks(Long adminUserId, int page, int size,
                                                       String status, String keyword) {
        requireAdmin(adminUserId);

        Specification<Task> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null && !status.isBlank()) {
                try {
                    predicates.add(cb.equal(root.get("status"), Task.TaskStatus.valueOf(status)));
                } catch (IllegalArgumentException ignored) {}
            }
            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Task> taskPage = taskRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "publishedAt")));

        List<TaskManagementDTO> items = taskPage.getContent().stream()
                .map(this::toTaskDTO).collect(Collectors.toList());

        return new PageResult<>(items, taskPage.getTotalElements(), page, size);
    }

    @Transactional
    public void deleteTask(Long adminUserId, Long taskId) {
        requireAdmin(adminUserId);
        if (!taskRepository.existsById(taskId)) {
            throw new BusinessException(404, "需求不存在");
        }
        taskRepository.deleteById(taskId);
    }

    // ==================== 分类管理 ====================

    @Transactional(readOnly = true)
    public List<CategoryManagementDTO> listCategories(Long adminUserId) {
        requireAdmin(adminUserId);
        return categoryRepository.findAll().stream()
                .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                .map(c -> CategoryManagementDTO.builder()
                        .id(c.getId()).name(c.getName()).icon(c.getIcon())
                        .hasReview(c.getHasReview()).sortOrder(c.getSortOrder()).build())
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryManagementDTO createCategory(Long adminUserId, CategoryUpsertRequest req) {
        requireAdmin(adminUserId);
        Category c = Category.builder()
                .name(req.getName()).icon(req.getIcon())
                .hasReview(req.getHasReview() != null && req.getHasReview())
                .sortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0)
                .build();
        c = categoryRepository.save(c);
        return CategoryManagementDTO.builder()
                .id(c.getId()).name(c.getName()).icon(c.getIcon())
                .hasReview(c.getHasReview()).sortOrder(c.getSortOrder()).build();
    }

    @Transactional
    public CategoryManagementDTO updateCategory(Long adminUserId, Integer categoryId,
                                                 CategoryUpsertRequest req) {
        requireAdmin(adminUserId);
        Category c = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(404, "分类不存在"));
        if (req.getName() != null) c.setName(req.getName());
        if (req.getIcon() != null) c.setIcon(req.getIcon());
        if (req.getHasReview() != null) c.setHasReview(req.getHasReview());
        if (req.getSortOrder() != null) c.setSortOrder(req.getSortOrder());
        c = categoryRepository.save(c);
        return CategoryManagementDTO.builder()
                .id(c.getId()).name(c.getName()).icon(c.getIcon())
                .hasReview(c.getHasReview()).sortOrder(c.getSortOrder()).build();
    }

    @Transactional
    public void deleteCategory(Long adminUserId, Integer categoryId) {
        requireAdmin(adminUserId);
        if (!categoryRepository.existsById(categoryId)) {
            throw new BusinessException(404, "分类不存在");
        }
        categoryRepository.deleteById(categoryId);
    }

    // ==================== 系统通知 ====================

    @Transactional(readOnly = true)
    public PageResult<NotificationDTO> listNotifications(Long adminUserId, int page, int size) {
        requireAdmin(adminUserId);
        Page<Notification> nPage = notificationRepository.findAll(
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime")));
        List<NotificationDTO> items = nPage.getContent().stream()
                .map(n -> NotificationDTO.builder()
                        .id(n.getId()).receiverId(n.getReceiverId())
                        .title(n.getTitle()).content(n.getContent())
                        .type(n.getType()).isRead(n.getIsRead())
                        .createTime(n.getCreateTime()).readTime(n.getReadTime()).build())
                .collect(Collectors.toList());
        return new PageResult<>(items, nPage.getTotalElements(), page, size);
    }

    @Transactional
    public void sendNotification(Long adminUserId, SystemNotificationRequest req) {
        requireAdmin(adminUserId);
        if (req.getReceiverId() != null) {
            Notification n = new Notification();
            n.setReceiverId(req.getReceiverId());
            n.setTitle(req.getTitle());
            n.setContent(req.getContent());
            n.setType(req.getType() != null ? req.getType() : "SYSTEM");
            notificationRepository.save(n);
        } else {
            List<User> allUsers = userRepository.findAll();
            List<Notification> batch = allUsers.stream().map(u -> {
                Notification n = new Notification();
                n.setReceiverId(u.getId());
                n.setTitle(req.getTitle());
                n.setContent(req.getContent());
                n.setType(req.getType() != null ? req.getType() : "SYSTEM");
                return n;
            }).collect(Collectors.toList());
            notificationRepository.saveAll(batch);
        }
    }

    // ==================== DTO 转换 ====================

    private UserManagementDTO toUserDTO(User u) {
        String phoneMasked = "";
        if (u.getPhone() != null && u.getPhone().length() >= 7) {
            phoneMasked = u.getPhone().substring(0, 3) + "****"
                    + u.getPhone().substring(u.getPhone().length() - 4);
        }
        return UserManagementDTO.builder()
                .id(u.getId()).name(u.getName()).nickname(u.getNickname())
                .email(u.getEmail()).phoneMasked(phoneMasked)
                .role(u.getRole() != null ? u.getRole().name() : "")
                .creditScore(u.getCreditScore()).status(u.getStatus())
                .createTime(u.getCreateTime()).build();
    }

    private TaskManagementDTO toTaskDTO(Task t) {
        return TaskManagementDTO.builder()
                .id(t.getId()).title(t.getTitle()).description(t.getDescription())
                .categoryName(t.getCategory() != null ? t.getCategory().getName() : "")
                .reward(t.getReward())
                .status(t.getStatus() != null ? t.getStatus().name() : "")
                .location(t.getLocation()).deadline(t.getDeadline())
                .publishedAt(t.getPublishedAt())
                .requesterId(t.getRequester() != null ? t.getRequester().getId() : null)
                .requesterName(t.getRequester() != null ? t.getRequester().getName() : "")
                .requesterEmail(t.getRequester() != null ? t.getRequester().getEmail() : "")
                .build();
    }
}
