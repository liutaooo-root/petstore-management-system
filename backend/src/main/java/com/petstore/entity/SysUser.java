package com.petstore.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/** 系统用户（管理员/店员） */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "登录名不能为空")
    private String username;

    /** BCrypt 摘要，接口层永不返回 */
    private String password;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    /** ADMIN 管理员 / STAFF 店员 */
    @NotBlank(message = "角色不能为空")
    private String role;

    private String phone;

    /** 1 启用 0 停用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
