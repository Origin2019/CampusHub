package com.campushub.common;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PageResult<T> {
    private List<T> items;
    private Pagination pagination;

    public PageResult(List<T> items, long total, int pageNum, int pageSize) {
        this.items = items;
        this.pagination = new Pagination(total, pageNum, pageSize);
    }

    public static <T> PageResult<T> of(Page<T> page) {
        return new PageResult<>(page.getContent(), page.getTotalElements(), page.getNumber() + 1, page.getSize());
    }

    @Data
    public static class Pagination {
        private long total;
        private int pageNum;
        private int pageSize;
        private int totalPages;

        public Pagination(long total, int pageNum, int pageSize) {
            this.total = total;
            this.pageNum = pageNum;
            this.pageSize = pageSize;
            this.totalPages = (int) Math.ceil((double) total / pageSize);
        }
    }
}