package com.paygateway.admin.controller;

import com.paygateway.admin.dto.*;
import com.paygateway.admin.service.DashboardService;
import com.paygateway.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "仪表盘接口")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @Operation(summary = "获取仪表盘统计数据")
    public Result<DashboardStats> getStats() {
        DashboardStats stats = dashboardService.getStats();
        return Result.success(stats);
    }

    @GetMapping("/trend")
    @Operation(summary = "获取趋势数据")
    public Result<DashboardTrend> getTrend(@RequestParam(defaultValue = "24h") String type) {
        DashboardTrend trend = dashboardService.getTrend(type);
        return Result.success(trend);
    }

    @GetMapping("/recent-transactions")
    @Operation(summary = "获取最近交易")
    public Result<List<RecentTransaction>> getRecentTransactions() {
        List<RecentTransaction> transactions = dashboardService.getRecentTransactions();
        return Result.success(transactions);
    }

    @GetMapping("/channels")
    @Operation(summary = "获取通道状态")
    public Result<List<ChannelStatus>> getChannels() {
        List<ChannelStatus> channels = dashboardService.getChannels();
        return Result.success(channels);
    }

    @GetMapping("/alerts")
    @Operation(summary = "获取告警列表")
    public Result<AlertListResponse> getAlerts() {
        AlertListResponse alerts = dashboardService.getAlerts();
        return Result.success(alerts);
    }

    @GetMapping("/amount-distribution")
    @Operation(summary = "获取金额分布")
    public Result<List<AmountDistribution>> getAmountDistribution(@RequestParam(defaultValue = "channel") String dimension) {
        List<AmountDistribution> distribution = dashboardService.getAmountDistribution(dimension);
        return Result.success(distribution);
    }
}
