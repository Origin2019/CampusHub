package com.example.backend.controller;

import com.example.backend.model.entity.Order;
import com.example.backend.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 1. 承接互助需求（抢单）
     */
    @PostMapping("/accept")
    public ResponseEntity<?> acceptTask(@RequestParam Long taskId,
                                        @RequestHeader("X-User-Id") Long currentUserId) {
        try {
            Order order = orderService.acceptTask(taskId, currentUserId);
            return ResponseEntity.ok(buildResponse(200, "接单成功！", order));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(400).body(buildResponse(400, e.getMessage(), null));
        }
    }

    /**
     * 2. 确认完成订单
     */
    @PostMapping("/{orderId}/complete")
    public ResponseEntity<?> completeOrder(@PathVariable Long orderId,
                                           @RequestHeader("X-User-Id") Long currentUserId) {
        try {
            orderService.completeOrder(orderId, currentUserId);
            return ResponseEntity.ok(buildResponse(200, "订单已确认完成！", null));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(400).body(buildResponse(400, e.getMessage(), null));
        }
    }

    /**
     * 3. 取消订单
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId,
                                         @RequestHeader("X-User-Id") Long currentUserId) {
        try {
            orderService.cancelOrder(orderId, currentUserId);
            return ResponseEntity.ok(buildResponse(200, "订单取消成功！", null));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(400).body(buildResponse(400, e.getMessage(), null));
        }
    }

    // 辅助构建统一返回格式的方法
    private Map<String, Object> buildResponse(int code, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", code);
        response.put("message", message);
        if (data != null) {
            response.put("data", data);
        }
        return response;
    }
}