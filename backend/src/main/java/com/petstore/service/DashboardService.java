package com.petstore.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petstore.entity.Appointment;
import com.petstore.mapper.AppointmentMapper;
import com.petstore.mapper.CustomerMapper;
import com.petstore.mapper.DashboardMapper;
import com.petstore.mapper.PetMapper;
import com.petstore.vo.DashboardSummaryVO;
import com.petstore.vo.KV;
import com.petstore.vo.TopPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 数据看板统计（口径：仅已支付账单） */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final DateTimeFormatter DATE_KEY = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final DashboardMapper dashboardMapper;
    private final CustomerMapper customerMapper;
    private final PetMapper petMapper;
    private final AppointmentMapper appointmentMapper;

    public DashboardSummaryVO summary() {
        DashboardSummaryVO vo = new DashboardSummaryVO();
        vo.setTodayRevenue(dashboardMapper.todayRevenue());
        vo.setMonthRevenue(dashboardMapper.monthRevenue());
        vo.setTodayBillCount(dashboardMapper.todayBillCount());
        vo.setMonthBillCount(dashboardMapper.monthBillCount());
        vo.setCustomerTotal(customerMapper.selectCount(null));
        vo.setPetTotal(petMapper.selectCount(null));
        vo.setNewCustomerThisMonth(dashboardMapper.newCustomerThisMonth());
        vo.setPendingAppointments(appointmentMapper.selectCount(
                new LambdaQueryWrapper<Appointment>().in(Appointment::getStatus, "BOOKED", "IN_PROGRESS")));
        return vo;
    }

    /** 近 N 天营收趋势，无单日补 0，保证折线连续 */
    public List<KV> revenueTrend(int days) {
        if (days <= 0 || days > 90) {
            days = 30;
        }
        LocalDate begin = LocalDate.now().minusDays(days - 1L);
        Map<String, BigDecimal> amounts = dashboardMapper.revenueTrend(begin.atStartOfDay()).stream()
                .collect(Collectors.toMap(KV::getK, KV::getV));
        List<KV> result = new ArrayList<>();
        for (LocalDate d = begin; !d.isAfter(LocalDate.now()); d = d.plusDays(1)) {
            String key = d.format(DATE_KEY);
            result.add(new KV(key, amounts.getOrDefault(key, BigDecimal.ZERO)));
        }
        return result;
    }

    /** 商品/服务营收占比（k: PRODUCT/SERVICE，前端映射中文） */
    public List<KV> revenueRatio() {
        return dashboardMapper.revenueRatio();
    }

    public List<TopPoint> topProducts(int limit) {
        return dashboardMapper.topItems(safeLimit(limit), "PRODUCT");
    }

    public List<TopPoint> topServices(int limit) {
        return dashboardMapper.topItems(safeLimit(limit), "SERVICE");
    }

    private int safeLimit(int limit) {
        return limit <= 0 || limit > 20 ? 5 : limit;
    }
}
