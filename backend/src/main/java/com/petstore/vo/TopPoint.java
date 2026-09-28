package com.petstore.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** 排行榜条目（k: 名称，qty: 总数量，v: 总金额） */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopPoint {

    private String k;
    private BigDecimal qty;
    private BigDecimal v;
}
