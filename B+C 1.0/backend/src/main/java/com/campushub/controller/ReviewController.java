package com.campushub.controller;

import com.campushub.common.ApiResponse;
import com.campushub.dto.ReviewRequest;
import com.campushub.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 评价 REST 控制器.
 * <p>
 * 评价接口挂载在订单路径下，语义上属于订单的子资源。
 * POST /v1/orders/{orderId}/review — 提交评价
 * </p>
 */
@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 提交评价（需求方对服务方评价，订单完成后可调用）.
     * POST /v1/orders/{orderId}/review
     */
    @PostMapping("/{orderId}/review")
    public ApiResponse<Void> submitReview(
            @PathVariable Long orderId,
            @Valid @RequestBody ReviewRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        reviewService.submitReview(orderId, userId, request.getScore(), request.getComment());
        return ApiResponse.success();
    }
}
