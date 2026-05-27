package com.campushub.controller;

import com.campushub.common.ApiResponse;
import com.campushub.dto.OrderDTO;
import com.campushub.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 订单 REST 控制器.
 * <p>
 * 接口前缀 /v1/orders，所有接口需登录（当前用 X-User-Id Header 模拟，后续替换为 JWT）。
 * Controller 层不包含业务逻辑，仅负责参数提取和响应格式化。
 * </p>
 */
@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 接单（抢单）.
     * POST /v1/orders/accept/{taskId}
     */
    @PostMapping("/accept/{taskId}")
    public ApiResponse<OrderDTO> acceptTask(
            @PathVariable Long taskId,
            @RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.success(orderService.acceptTask(taskId, userId));
    }

    /**
     * 查看订单详情（仅需求方和服务方可见）.
     * GET /v1/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    public ApiResponse<OrderDTO> getOrderDetail(
            @PathVariable Long orderId,
            @RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.success(orderService.getOrderDetail(orderId, userId));
    }

    /**
     * 确认完成订单（仅需求方可操作）.
     * POST /v1/orders/{orderId}/complete
     */
    @PostMapping("/{orderId}/complete")
    public ApiResponse<Void> completeOrder(
            @PathVariable Long orderId,
            @RequestHeader("X-User-Id") Long userId) {
        orderService.completeOrder(orderId, userId);
        return ApiResponse.success();
    }

    /**
     * 取消订单（需求方或服务方均可操作，结果不同）.
     * POST /v1/orders/{orderId}/cancel
     */
    @PostMapping("/{orderId}/cancel")
    public ApiResponse<Void> cancelOrder(
            @PathVariable Long orderId,
            @RequestHeader("X-User-Id") Long userId) {
        orderService.cancelOrder(orderId, userId);
        return ApiResponse.success();
    }
}
