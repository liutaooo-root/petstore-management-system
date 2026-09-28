package com.petstore.vo;

import com.petstore.entity.Bill;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 账单列表/详情视图：附加客户、宠物、操作员姓名 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BillVO extends Bill {

    private String customerName;

    private String petName;

    private String operatorName;
}
