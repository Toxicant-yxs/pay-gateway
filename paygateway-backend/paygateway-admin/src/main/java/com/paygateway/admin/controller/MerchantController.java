package com.paygateway.admin.controller;

import com.paygateway.admin.entity.MerchantInfo;
import com.paygateway.admin.service.MerchantService;
import com.paygateway.common.dto.PageQuery;
import com.paygateway.common.result.PageResult;
import com.paygateway.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/merchants")
@RequiredArgsConstructor
@Tag(name = "商户管理接口")
public class MerchantController {

    private final MerchantService merchantService;

    @GetMapping
    @Operation(summary = "分页查询商户列表")
    public Result<PageResult<MerchantInfo>> list(
            @Parameter(description = "页码") @RequestParam(required = false) Integer page,
            @Parameter(description = "每页数量") @RequestParam(required = false) Integer pageSize,
            @Parameter(description = "商户名称") @RequestParam(required = false) String merchantName,
            @Parameter(description = "商户编号") @RequestParam(required = false) String merchantNo,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        PageQuery pageQuery = new PageQuery();
        if (page != null) pageQuery.setPage(page);
        if (pageSize != null) pageQuery.setPageSize(pageSize);
        PageResult<MerchantInfo> result = merchantService.list(pageQuery, merchantName, merchantNo, keyword, status, startTime, endTime);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取商户详情")
    public Result<MerchantInfo> getById(@PathVariable Long id) {
        MerchantInfo merchant = merchantService.getById(id);
        return Result.success(merchant);
    }

    @PostMapping
    @Operation(summary = "创建商户")
    public Result<MerchantInfo> create(@RequestBody MerchantInfo merchant) {
        return Result.success(merchantService.create(merchant));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新商户")
    public Result<MerchantInfo> update(@PathVariable Long id, @RequestBody MerchantInfo merchant) {
        return Result.success(merchantService.update(id, merchant));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新商户状态")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        merchantService.updateStatus(id, request.getStatus(), request.getReason());
        return Result.success();
    }

    @GetMapping("/audits")
    @Operation(summary = "获取审核商户列表")
    public Result<PageResult<MerchantInfo>> getAuditList(
            @Parameter(description = "页码") @RequestParam(required = false) Integer page,
            @Parameter(description = "每页数量") @RequestParam(required = false) Integer pageSize) {
        PageQuery pageQuery = new PageQuery();
        if (page != null) pageQuery.setPage(page);
        if (pageSize != null) pageQuery.setPageSize(pageSize);
        return Result.success(merchantService.getAuditList(pageQuery));
    }

    @PostMapping("/audits/{id}/approve")
    @Operation(summary = "审核通过")
    public Result<Void> approve(@PathVariable Long id) {
        merchantService.approve(id);
        return Result.success();
    }

    @PostMapping("/audits/{id}/reject")
    @Operation(summary = "审核拒绝")
    public Result<Void> reject(@PathVariable Long id, @RequestBody RejectRequest request) {
        merchantService.reject(id, request.getReason());
        return Result.success();
    }

    @GetMapping("/audits/stats")
    @Operation(summary = "获取审核统计")
    public Result<Map<String, Object>> getAuditStats() {
        return Result.success(merchantService.getAuditStats());
    }

    @GetMapping("/status-count")
    @Operation(summary = "获取状态统计")
    public Result<Map<String, Object>> getStatusCount() {
        return Result.success(merchantService.getStatusCount());
    }

    @PutMapping("/{id}/audit")
    @Operation(summary = "审核商户")
    public Result<Void> audit(@PathVariable Long id, @RequestBody AuditRequest request) {
        merchantService.audit(id, request.getStatus(), request.getRemark());
        return Result.success();
    }

    @Data
    public static class AuditRequest {
        private Integer status;
        private String remark;
    }

    @Data
    public static class StatusRequest {
        private Integer status;
        private String reason;
    }

    @Data
    public static class RejectRequest {
        private String reason;
    }
}
