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

/** 服务项目（洗护、寄养等） */
@Data
@TableName("service_item")
public class ServiceItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "服务名称不能为空")
    private String name;

    @NotNull(message = "服务价格不能为空")
    private BigDecimal price;

    /** 服务时长（分钟） */
    private Integer durationMin;

    private String description;

    /** 1 上架 0 下架 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
