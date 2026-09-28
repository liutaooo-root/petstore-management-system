package com.petstore.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.List;
import java.util.function.Function;

/** 分页响应体 */
@Data
public class PageResult<T> {

    private List<T> records;
    private long total;
    private long current;
    private long size;

    /** 实体分页直接转（或转换元素类型） */
    public static <S, T> PageResult<T> of(Page<S> page, Function<S, T> converter) {
        PageResult<T> r = new PageResult<>();
        r.setRecords(page.getRecords().stream().map(converter).toList());
        r.setTotal(page.getTotal());
        r.setCurrent(page.getCurrent());
        r.setSize(page.getSize());
        return r;
    }

    /** 已组装好的 VO 列表 + 原分页信息 */
    public static <T> PageResult<T> of(List<T> records, Page<?> page) {
        PageResult<T> r = new PageResult<>();
        r.setRecords(records);
        r.setTotal(page.getTotal());
        r.setCurrent(page.getCurrent());
        r.setSize(page.getSize());
        return r;
    }
}
