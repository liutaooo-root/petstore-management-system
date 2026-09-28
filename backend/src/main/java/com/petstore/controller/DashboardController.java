package com.petstore.controller;

import com.petstore.common.Result;
import com.petstore.service.DashboardService;
import com.petstore.vo.DashboardSummaryVO;
import com.petstore.vo.KV;
import com.petstore.vo.TopPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public Result<DashboardSummaryVO> summary() {
        return Result.ok(dashboardService.summary());
    }

    @GetMapping("/revenue-trend")
    public Result<List<KV>> revenueTrend(@RequestParam(defaultValue = "30") int days) {
        return Result.ok(dashboardService.revenueTrend(days));
    }

    @GetMapping("/revenue-ratio")
    public Result<List<KV>> revenueRatio() {
        return Result.ok(dashboardService.revenueRatio());
    }

    @GetMapping("/top-products")
    public Result<List<TopPoint>> topProducts(@RequestParam(defaultValue = "5") int limit) {
        return Result.ok(dashboardService.topProducts(limit));
    }

    @GetMapping("/top-services")
    public Result<List<TopPoint>> topServices(@RequestParam(defaultValue = "5") int limit) {
        return Result.ok(dashboardService.topServices(limit));
    }
}
