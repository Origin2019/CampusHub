package com.example.backend.model.enums;

public enum OrderStatus {
    accepted,    // 已接单
    completed,   // 已完成（需求方确认送达）
    cancelled,   // 已取消
    evaluated    // 已评价（双方互评结束，订单最终完结终态）
}