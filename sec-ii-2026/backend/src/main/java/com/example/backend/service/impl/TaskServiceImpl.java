package com.example.backend.service.impl;

import com.example.backend.mapper.TaskMapper;
import com.example.backend.model.entity.Task;
import com.example.backend.model.enums.TaskStatus;
import com.example.backend.service.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskMapper taskMapper;

    public TaskServiceImpl(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Task publishTask(Task task) {
        // 1. 严格的安全业务校验：截止时间（deadline）不能早于当前时间
        if (task.getDeadline() == null || task.getDeadline().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("参数错误：任务截止时间不能早于当前系统时间！");
        }

        // 2. 悬赏金额校验：不能为负数
        if (task.getReward() == null || task.getReward().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("参数错误：悬赏金额不能为负数！");
        }

        // 3. 补全初始状态
        task.setStatus(TaskStatus.published);

        // 4. 写入数据库
        taskMapper.insertTask(task);
        return task;
    }

    @Override
    public List<Task> getTaskList(Integer categoryId, String status) {
        // 直接调用 Mapper 的动态 SQL 查询并返回
        return taskMapper.selectTaskList(categoryId, status);
    }
}