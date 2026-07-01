package com.paygateway.admin.controller;

import com.paygateway.admin.entity.RiskEvent;
import com.paygateway.admin.entity.RiskRule;
import com.paygateway.admin.service.RiskService;
import com.paygateway.common.dto.PageQuery;
import com.paygateway.common.result.PageResult;
import com.paygateway.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/risk")
@RequiredArgsConstructor
@Tag(name = "风控管理接口")
public class RiskController {

    private final RiskService riskService;

    @GetMapping("/rules")
    @Operation(summary = "查询风控规则列表")
    public Result<PageResult<RiskRule>> listRules(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status) {
        PageQuery pageQuery = new PageQuery();
        if (page != null) pageQuery.setPage(page);
        if (pageSize != null) pageQuery.setPageSize(pageSize);
        return Result.success(riskService.listRules(pageQuery, category, status));
    }

    @PostMapping("/rules")
    @Operation(summary = "创建风控规则")
    public Result<RiskRule> createRule(@RequestBody RiskRule rule) {
        return Result.success(riskService.createRule(rule));
    }

    @PutMapping("/rules/{id}")
    @Operation(summary = "更新风控规则")
    public Result<RiskRule> updateRule(@PathVariable Long id, @RequestBody RiskRule rule) {
        return Result.success(riskService.updateRule(id, rule));
    }

    @PutMapping("/rules/{id}/status")
    @Operation(summary = "更新规则状态")
    public Result<Void> updateRuleStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        riskService.updateRuleStatus(id, request.getStatus());
        return Result.success();
    }

    @GetMapping("/events")
    @Operation(summary = "查询风险事件列表")
    public Result<PageResult<RiskEvent>> listEvents(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String merchantNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        PageQuery pageQuery = new PageQuery();
        if (page != null) pageQuery.setPage(page);
        if (pageSize != null) pageQuery.setPageSize(pageSize);
        return Result.success(riskService.listEvents(pageQuery, category, riskLevel, merchantNo, status, startTime, endTime));
    }

    @GetMapping("/events/{id}")
    @Operation(summary = "获取事件详情")
    public Result<RiskEvent> getEventById(@PathVariable Long id) {
        return Result.success(riskService.getEventById(id));
    }

    @PostMapping("/events/{id}/handle")
    @Operation(summary = "处理风险事件")
    public Result<Void> handleEvent(@PathVariable Long id, @RequestBody HandleRequest request) {
        riskService.handleEvent(id, request.getHandleNote());
        return Result.success();
    }

    @GetMapping("/events/stats")
    @Operation(summary = "获取风险事件统计")
    public Result<Map<String, Object>> getEventStats() {
        return Result.success(riskService.getEventStats());
    }

    @Data
    public static class StatusRequest {
        private Integer status;
    }

    @Data
    public static class HandleRequest {
        private String handleNote;
    }
}
