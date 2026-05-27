package com.example.backend.controller;

import com.example.backend.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * 提交订单评价
     * 接收参数：订单ID、评分星级、评语，并从 Header 模拟获取当前登录用户ID
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitReview(@RequestParam Long orderId,
                                          @RequestParam Integer rating,
                                          @RequestParam(required = false, defaultValue = "系统默认好评") String comment,
                                          @RequestHeader("X-User-Id") Long currentUserId) {
        try {
            reviewService.submitReview(orderId, currentUserId, rating, comment);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "评价提交成功，信用分已同步更新！");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }
}