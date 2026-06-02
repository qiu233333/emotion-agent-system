package com.bishe.backend.dto;

import lombok.Data;

import java.util.List;

/**
 * 分页响应对象。
 *
 * <p>后台管理端的用户列表和日记列表都需要分页展示，因此统一使用该对象返回
 * 当前页数据、总数量、当前页码和每页条数。</p>
 *
 * @param <T> 当前分页列表中的记录类型
 */
@Data
public class PageResponse<T> {

    /**
     * 当前页记录列表。
     */
    private List<T> records;

    /**
     * 满足查询条件的总记录数。
     */
    private Long total;

    /**
     * 当前页码，从 1 开始。
     */
    private Long page;

    /**
     * 每页记录数量。
     */
    private Long size;

    /**
     * 创建分页响应对象。
     *
     * @param records 当前页记录列表
     * @param total 满足条件的总记录数
     * @param page 当前页码
     * @param size 每页记录数量
     * @param <T> 当前分页列表中的记录类型
     * @return 分页响应对象
     */
    public static <T> PageResponse<T> of(List<T> records, Long total, Long page, Long size) {
        PageResponse<T> response = new PageResponse<>();
        response.setRecords(records);
        response.setTotal(total);
        response.setPage(page);
        response.setSize(size);
        return response;
    }
}
