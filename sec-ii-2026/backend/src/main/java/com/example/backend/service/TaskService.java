package com.example.backend.service;

import com.example.backend.model.entity.Task;
import java.util.List;

public interface TaskService {
    /**
     * 发布新的互助需求
     */
    Task publishTask(Task task);

    /**
     * 动态条件查询任务列表
     * @param categoryId 分类ID（可选）
     * @param status 状态（可选）
     */
    List<Task> getTaskList(Integer categoryId, String status);
}