package com.campushub.repository;

import com.campushub.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 需求数据访问层接口.
 * <p>
 * 继承 JpaRepository（CRUD）+ JpaSpecificationExecutor（动态条件查询）。
 * 额外提供两个原子更新方法，供接单、完成、取消流程使用。
 * </p>
 */
public interface TaskRepository extends JpaRepository<Task, Long>,
        JpaSpecificationExecutor<Task> {

    /**
     * 按 ID 查询需求并立即加载发布者和分类（JOIN FETCH，避免 N+1）.
     */
    @Query("SELECT t FROM Task t JOIN FETCH t.requester JOIN FETCH t.category WHERE t.id = :id")
    Optional<Task> findByIdWithDetails(@Param("id") Long id);

    /**
     * 接单时的并发控制核心 SQL.
     * 仅当 status = 'published' 时才能更新为 'in_progress'，
     * 利用数据库原子性防止多人同时抢单成功。
     * 返回受影响行数：1=抢单成功，0=已被他人抢走。
     */
    @Modifying
    @Query(value = "UPDATE tasks SET status = 'in_progress', updated_at = NOW() " +
                   "WHERE id = :taskId AND status = 'published'", nativeQuery = true)
    int updateStatusForAccepting(@Param("taskId") Long taskId);

    /**
     * 通用状态更新（用于完成、取消、回滚到 published 等场景）.
     *
     * @param taskId 需求主键
     * @param status 目标状态字符串：published / completed / cancelled
     */
    @Modifying
    @Query(value = "UPDATE tasks SET status = :status, updated_at = NOW() WHERE id = :taskId",
           nativeQuery = true)
    int updateStatus(@Param("taskId") Long taskId, @Param("status") String status);
}
