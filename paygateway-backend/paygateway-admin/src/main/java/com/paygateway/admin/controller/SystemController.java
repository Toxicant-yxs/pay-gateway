package com.paygateway.admin.controller;

import com.paygateway.admin.entity.OperationLog;
import com.paygateway.admin.entity.SystemConfig;
import com.paygateway.admin.entity.SystemNotification;
import com.paygateway.admin.service.SystemService;
import com.paygateway.common.dto.PageQuery;
import com.paygateway.common.result.PageResult;
import com.paygateway.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "系统管理接口")
public class SystemController {

    private final SystemService systemService;

    @GetMapping("/system/config")
    @Operation(summary = "获取系统配置")
    public Result<Map<String, Object>> getConfig() {
        return Result.success(systemService.getConfig());
    }

    @PutMapping("/system/config")
    @Operation(summary = "更新系统配置")
    public Result<Void> updateConfig(@RequestBody List<SystemConfig> configs) {
        systemService.updateConfig(configs);
        return Result.success();
    }

    @GetMapping("/notifications/unread-count")
    @Operation(summary = "获取未读通知数量")
    public Result<Long> getUnreadCount() {
        return Result.success(systemService.getUnreadCount());
    }

    @GetMapping("/notifications")
    @Operation(summary = "获取通知列表")
    public Result<PageResult<SystemNotification>> getNotifications(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String category) {
        PageQuery pageQuery = new PageQuery();
        if (page != null) pageQuery.setPage(page);
        if (pageSize != null) pageQuery.setPageSize(pageSize);
        return Result.success(systemService.getNotifications(pageQuery, category));
    }

    @PutMapping("/notifications/{id}/read")
    @Operation(summary = "标记通知已读")
    public Result<Void> markRead(@PathVariable Long id) {
        systemService.markRead(id);
        return Result.success();
    }

    @PutMapping("/notifications/read-all")
    @Operation(summary = "全部标记已读")
    public Result<Void> markAllRead() {
        systemService.markAllRead();
        return Result.success();
    }

    @GetMapping("/system/logs")
    @Operation(summary = "查询操作日志")
    public Result<PageResult<OperationLog>> getOperationLogs(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        PageQuery pageQuery = new PageQuery();
        if (page != null) pageQuery.setPage(page);
        if (pageSize != null) pageQuery.setPageSize(pageSize);
        return Result.success(systemService.getOperationLogs(pageQuery, username, startTime, endTime));
    }

    @GetMapping("/search")
    @Operation(summary = "全局搜索")
    public Result<Map<String, Object>> globalSearch(
            @RequestParam String keyword,
            @RequestParam(required = false) String types) {
        return Result.success(systemService.globalSearch(keyword, types));
    }
}
