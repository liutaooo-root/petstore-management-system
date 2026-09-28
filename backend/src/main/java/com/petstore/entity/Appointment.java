package com.petstore.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/** 服务预约单 */
@Data
@TableName("appointment")
public class Appointment {

    @TableId(type = IdType.AUTO)
    private Long id;

    @NotNull(message = "请选择客户")
    private Long customerId;

    @NotNull(message = "请选择宠物")
    private Long petId;

    @NotNull(message = "请选择服务项目")
    private Long serviceId;

    /** 接待/负责员工 */
    private Long staffId;

    @NotNull(message = "请选择预约时间")
    private LocalDateTime appointmentTime;

    /** BOOKED 待到店 / IN_PROGRESS 进行中 / COMPLETED 已完成 / CANCELLED 已取消 */
    private String status;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
