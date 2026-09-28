package com.petstore.vo;

import lombok.Data;

import java.math.BigDecimal;

/** 看板顶部汇总指标 */
@Data
public class DashboardSummaryVO {

    private BigDecimal todayRevenue;

    private BigDecimal monthRevenue;

    private long todayBillCount;

    private long monthBillCount;

    private long customerTotal;

    private long petTotal;

    private long newCustomerThisMonth;

    private long pendingAppointments;
}
