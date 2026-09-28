package com.petstore.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 账单（一次交易：商品/服务/混合） */
@Data
@TableName("bill")
public class Bill {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 单号：B + 时间戳 + 随机数，唯一 */
    private String billNo;

    private Long customerId;

    /** 本次服务对象（可选） */
    private Long petId;

    /** PRODUCT / SERVICE / MIXED */
    private String type;

    private BigDecimal totalAmount;

    /** 整单优惠（抹零/折扣） */
    private BigDecimal discountAmount;

    /** 实收金额 = total - discount */
    private BigDecimal payAmount;

    /** CASH / WECHAT / ALIPAY / CARD */
    private String payMethod;

    /** UNPAID 待支付 / PAID 已支付 / CANCELLED 已取消 */
    private String status;

    /** 操作员（登录用户） */
    private Long operatorId;

    private String remark;

    private LocalDateTime payTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
