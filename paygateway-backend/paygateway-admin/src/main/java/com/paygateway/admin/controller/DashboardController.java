package com.paygateway.admin.controller;

import com.paygateway.admin.dto.DashboardStats;
import com.paygateway.admin.service.DashboardService;
import com.paygateway.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
