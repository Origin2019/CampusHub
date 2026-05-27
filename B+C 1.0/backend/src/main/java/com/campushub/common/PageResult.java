package com.campushub.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 分页查询结果通用包装类.
 * <p>
 * 用于包裹所有需要分页返回的接口响应，
 * 包含当前页数据列表和分页元信息。
 * </p>
 * <br>
 * JSON 输出示例：
 * <pre>
 * {
 *   "items": [ ... ],
 *   "pagination": {
 *     "page": 1,
 *     "pageSize": 20,
 *     "total": 45,
 *     "totalPages": 3
 *   }
 * }
 * </pre>
 *
 * @param <T> items 列表中每条记录的类型
 */
@Data
@AllArgsConstructor
public class PageResult<T> {

    /** 当前页的数据列表 */
    private List<T> items;

    /** 分页元信息 */
    private Pagination pagination;

    /**
     * 分页元信息内部类.
     * <p>
     * 包含页码、每页大小、总记录数、总页数四个字段，
     * 前端可根据这些数据渲染分页导航条。
     * </p>
     */
    @Data
    @AllArgsConstructor
    public static class Pagination {

        /** 当前页码，从 1 开始 */
        private int page;

        /** 每页最大记录条数 */
        private int pageSize;

        /** 符合筛选条件的总记录条数 */
        private long total;

        /** 总页数，由 total 和 pageSize 计算得出 */
        private int totalPages;
    }
}
