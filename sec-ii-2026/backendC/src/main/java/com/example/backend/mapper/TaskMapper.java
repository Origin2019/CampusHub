package com.example.backend.mapper;

import com.example.backend.model.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TaskMapper {

    /**
     * 根据需求ID查询详情
     */
    @Select("SELECT * FROM tasks WHERE id = #{id}")
    Task selectById(@Param("id") Long id);

    /**
     * 并发控制核心 SQL：利用数据库原子性更新
     * 只有当前状态是 'published'（已发布）时，才允许将其修改为 'in_progress'（进行中）
     * 如果另外一个线程提前修改了状态，当前线程的这条 SQL 影响行数（affected rows）就会返回 0
     */
    @Update("UPDATE tasks SET status = 'in_progress', updated_at = NOW() " +
            "WHERE id = #{taskId} AND status = 'published'")
    int updateTaskStatusForAccepting(@Param("taskId") Long taskId);

    /**
     * 后续完成或取消订单时修改需求状态
     */
    @Update("UPDATE tasks SET status = #{status}, updated_at = NOW() WHERE id = #{taskId}")
    int updateTaskStatus(@Param("taskId") Long taskId, @Param("status") String status);

    /**
     * 插入新发布的互助需求任务
     */
    @org.apache.ibatis.annotations.Insert(
            "INSERT INTO tasks (requester_id, category_id, title, description, reward, status, location, deadline, published_at, created_at, updated_at) " +
                    "VALUES (#{requesterId}, #{categoryId}, #{title}, #{description}, #{reward}, 'published', #{location}, #{deadline}, NOW(), NOW(), NOW())"
    )
    @org.apache.ibatis.annotations.Options(useGeneratedKeys = true, keyProperty = "id")
    int insertTask(Task task);

    /**
     * 首页大厅多条件动态筛选列表（按时间倒序）
     * 使用 <script> 标签包裹，动态判断参数是否为空，如果为空就不拼接该 WHERE 条件
     */
    @Select("<script>" +
            "SELECT * FROM tasks " +
            "<where>" +
            "  <if test='categoryId != null'> AND category_id = #{categoryId} </if>" +
            "  <if test='status != null and status != \"\"'> AND status = #{status} </if>" +
            "</where>" +
            "ORDER BY published_at DESC" +
            "</script>")
    java.util.List<Task> selectTaskList(@Param("categoryId") Integer categoryId, @Param("status") String status);

    /**
     * 查询指定用户的发布历史（支持按状态筛选）
     */
    @Select("<script>" +
            "SELECT * FROM tasks " +
            "WHERE requester_id = #{userId} " +
            "  <if test='status != null and status != \"\"'> AND status = #{status} </if> " +
            "ORDER BY created_at DESC" +
            "</script>")
    java.util.List<Task> selectPublishHistory(@Param("userId") Long userId, @Param("status") String status);
}