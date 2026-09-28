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
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 宠物档案（归属客户） */
@Data
@TableName("pet")
public class Pet {

    @TableId(type = IdType.AUTO)
    private Long id;

    @NotNull(message = "请选择宠物主人")
    private Long customerId;

    @NotBlank(message = "宠物名不能为空")
    private String name;

    /** 狗/猫/兔/仓鼠/鸟类/其他 */
    private String species;

    private String breed;

    /** 1 公 2 母 */
    private Integer gender;

    private LocalDate birthDate;

    /** 体重（kg） */
    private BigDecimal weight;

    /** 0 否 1 是 */
    private Integer sterilized;

    private String notes;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
