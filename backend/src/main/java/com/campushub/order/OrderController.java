package com.campushub.order;

import com.campushub.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/accept")
    public ApiResponse<OrderDTO> acceptTask(@RequestParam Long taskId,
                                            @RequestHeader("X-User-Id") Long currentUserId) {
        return ApiResponse.success("接单成功", orderService.acceptTask(taskId, currentUserId));
    }

    @PostMapping("/{orderId}/complete")
    public ApiResponse<Void> completeOrder(@PathVariable Long orderId,
                                           @RequestHeader("X-User-Id") Long currentUserId) {
        orderService.completeOrder(orderId, currentUserId);
        return ApiResponse.success();
    }

    @PostMapping("/{orderId}/cancel")
    public ApiResponse<Void> cancelOrder(@PathVariable Long orderId,
                                         @RequestHeader("X-User-Id") Long currentUserId) {
        orderService.cancelOrder(orderId, currentUserId);
        return ApiResponse.success();
    }

    @GetMapping("/my-accepted")
    public ApiResponse<List<OrderDTO>> getMyAccepted(@RequestHeader("X-User-Id") Long currentUserId,
                                                     @RequestParam(required = false) String status) {
        return ApiResponse.success(orderService.getAcceptHistory(currentUserId, status));
    }
}