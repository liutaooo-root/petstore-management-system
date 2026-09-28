package com.petstore.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 账单明细：对商品/服务的名称与单价做快照，
 * 后续改价/改名不影响历史账单。
 */
@Data
@TableName("bill_item")
public class BillItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long billId;

    /** PRODUCT / SERVICE */
    private String itemType;

    /** 商品或服务 id（多态引用） */
    private Long refId;

    /** 快照名称 */
    private String itemName;

    /** 快照单价 */
    private BigDecimal unitPrice;

    private Integer quantity;

    /** 单项小计 = 单价 × 数量 */
    private BigDecimal amount;
}
