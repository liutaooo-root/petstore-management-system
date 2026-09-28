package com.petstore.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 商品（含库存） */
@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    @NotNull(message = "请选择商品分类")
    private Long categoryId;

    @NotBlank(message = "商品名称不能为空")
    private String name;

    /** 规格，如 1.5kg */
    private String spec;

    /** 单位：袋/个/瓶/盒 */
    private String unit;

    @NotNull(message = "售价不能为空")
    private BigDecimal price;

    /** 进货成本 */
    private BigDecimal cost;

    private Integer stock;

    /** 1 上架 0 下架 */
    private Integer status;

    private String notes;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
