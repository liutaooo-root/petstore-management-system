package com.petstore.vo;

import com.petstore.entity.Appointment;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 预约列表视图：附加客户/宠物/服务/员工姓名 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AppointmentVO extends Appointment {

    private String customerName;

    private String petName;

    private String serviceName;

    private String staffName;
}
