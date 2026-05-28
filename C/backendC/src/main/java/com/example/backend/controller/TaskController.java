package com.example.backend.controller;

import com.example.backend.model.entity.Task;
import com.example.backend.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * 1. 发布互助需求任务
     * 通过 Header 传入的 X-User-Id 模拟当前登录发布任务的需求方
     */
    @PostMapping("/publish")
    public ResponseEntity<?> publishTask(@RequestBody Task task,
                                         @RequestHeader("X-User-Id") Long currentUserId) {
        try {
            task.setRequesterId(currentUserId); // 绑定当前登录的用户 ID
            Task publishedTask = taskService.publishTask(task);
            return ResponseEntity.ok(buildResponse(200, "互助需求发布成功！", publishedTask));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(buildResponse(400, e.getMessage(), null));
        }
    }

    /**
     * 2. 首页大厅多条件动态筛选列表查询
     * 参数均为可选：/api/tasks/list?categoryId=1&status=published
     */
    @GetMapping("/list")
    public ResponseEntity<?> getTaskList(@RequestParam(required = false) Integer categoryId,
                                         @RequestParam(required = false) String status) {
        List<Task> tasks = taskService.getTaskList(categoryId, status);
        return ResponseEntity.ok(buildResponse(200, "大厅列表查询成功", tasks));
    }

    private Map<String, Object> buildResponse(int code, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", code);
        response.put("message", message);
        if (data != null) {
            response.put("data", data);
        }
        return response;
    }

    @GetMapping("/my-published")
    public ResponseEntity<?> getMyPublished(@RequestHeader("X-User-Id") Long currentUserId,
                                            @RequestParam(required = false) String status) {
        List<Task> history = taskService.getPublishHistory(currentUserId, status);
        return ResponseEntity.ok(buildResponse(200, "查询发布历史成功", history));
    }
}