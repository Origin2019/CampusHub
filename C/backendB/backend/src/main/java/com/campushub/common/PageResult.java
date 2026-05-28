package com.campushub.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 分页查询结果通用包装类.
 */
@Data
@AllArgsConstructor
public class PageResult<T> {

    private List<T> items;
    private Pagination pagination;

    @Data
    @AllArgsConstructor
    public static class Pagination {
        private int page;
        private int pageSize;
        private long total;
        private int totalPages;
    }
}
