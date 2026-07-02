package com.paygateway.admin.controller;

import com.paygateway.admin.entity.ReconDetail;
import com.paygateway.admin.entity.ReconTask;
import com.paygateway.admin.service.ReconService;
import com.paygateway.common.dto.PageQuery;
import com.paygateway.common.result.PageResult;
import com.paygateway.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/reconciliation")
@RequiredArgsConstructor
@Tag(name = "对账管理接口")
public class ReconController {

    private final ReconService reconService;

    @GetMapping("/tasks")
    @Operation(summary = "查询对账任务列表")
    public Result<PageResult<ReconTask>> listTasks(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String channelCode,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        PageQuery pageQuery = new PageQuery();
        if (page != null) pageQuery.setPage(page);
        if (pageSize != null) pageQuery.setPageSize(pageSize);
        return Result.success(reconService.listTasks(pageQuery, channelCode, status, startDate, endDate));
    }

    @GetMapping("/tasks/{id}")
    @Operation(summary = "获取对账任务详情")
    public Result<ReconTask> getTaskById(@PathVariable Long id) {
        return Result.success(reconService.getTaskById(id));
    }

    @GetMapping("/tasks/{id}/diffs")
    @Operation(summary = "获取对账差异列表")
    public Result<PageResult<ReconDetail>> getTaskDiffs(
            @PathVariable Long id,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String diffType) {
        PageQuery pageQuery = new PageQuery();
        if (page != null) pageQuery.setPage(page);
        if (pageSize != null) pageQuery.setPageSize(pageSize);
        return Result.success(reconService.getTaskDiffs(id, pageQuery, diffType));
    }

    @PostMapping("/tasks/{id}/retry")
    @Operation(summary = "重试对账任务")
    public Result<Void> retryTask(@PathVariable Long id) {
        reconService.retryTask(id);
        return Result.success();
    }

    @PostMapping("/diffs/{id}/handle")
    @Operation(summary = "处理对账差异")
    public Result<Void> handleDiff(@PathVariable Long id, @RequestBody HandleDiffRequest request) {
        reconService.handleDiff(id, request.getAction(), request.getNote());
        return Result.success();
    }

    @GetMapping("/tasks/stats")
    @Operation(summary = "获取对账统计")
    public Result<Map<String, Object>> getStats() {
        return Result.success(reconService.getStats());
    }

    @GetMapping("/tasks/{id}/download")
    @Operation(summary = "下载对账账单")
    public void downloadBill(@PathVariable Long id, HttpServletResponse response) throws IOException {
        reconService.downloadBill(id, response);
    }

    @GetMapping("/reports")
    @Operation(summary = "查询对账报表")
    public Result<PageResult<Map<String, Object>>> getReports(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String channelCode) {
        PageQuery pageQuery = new PageQuery();
        if (page != null) pageQuery.setPage(page);
        if (pageSize != null) pageQuery.setPageSize(pageSize);
        return Result.success(reconService.getReports(pageQuery, startDate, endDate, channelCode));
    }

    @GetMapping("/reports/export")
    @Operation(summary = "导出对账报表")
    public void exportReport(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String channelCode,
            @RequestParam(required = false, defaultValue = "excel") String format,
            HttpServletResponse response) throws IOException {
        reconService.exportReport(startDate, endDate, channelCode, format, response);
    }

    @Data
    public static class HandleDiffRequest {
        private String action;
        private String note;
    }
}
