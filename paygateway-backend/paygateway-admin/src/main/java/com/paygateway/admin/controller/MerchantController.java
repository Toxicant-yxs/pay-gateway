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
import org.springframework.web.bind.annotation.*;

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
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        PageQuery pageQuery = new PageQuery();
        if (page != null) pageQuery.setPage(page);
        if (pageSize != null) pageQuery.setPageSize(pageSize);
        PageResult<MerchantInfo> result = merchantService.list(pageQuery, keyword, status);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取商户详情")
    public Result<MerchantInfo> getById(@PathVariable Long id) {
        MerchantInfo merchant = merchantService.getById(id);
        return Result.success(merchant);
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
}
