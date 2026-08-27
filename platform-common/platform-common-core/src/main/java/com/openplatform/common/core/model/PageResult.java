package com.openplatform.common.core.model;

import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一分页结果。
 *
 * @param <T> 分页记录类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private List<T> records;

    private long page;

    private long pageSize;

    private long total;

    public static <T> PageResult<T> of(List<T> records, long page, long pageSize, long total) {
        List<T> safeRecords = records == null ? Collections.emptyList() : List.copyOf(records);
        return new PageResult<>(safeRecords, page, pageSize, total);
    }

    public static <T> PageResult<T> empty(long page, long pageSize) {
        return new PageResult<>(Collections.emptyList(), page, pageSize, 0L);
    }
}
