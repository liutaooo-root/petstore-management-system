package com.petstore.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** 图表通用键值对（k: 日期/类型/名称，v: 金额） */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KV {

    private String k;
    private BigDecimal v;
}
