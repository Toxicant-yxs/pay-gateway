package com.paygateway.admin.controller;

import com.paygateway.admin.entity.RefundOrder;
import com.paygateway.admin.service.RefundOrderService;
import com.paygateway.common.dto.PageQuery;
import com.paygateway.common.result.PageResult;
import com.paygateway.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/refunds")
@RequiredArgsConstructor
@Tag(name = "退款订单接口")
public class RefundOrderController {

    private final RefundOrderService refundOrderService;

    @GetMapping
    @Operation(summary = "分页查询退款订单")
    public Result<PageResult<RefundOrder>> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String refundNo,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String merchantNo,
            @RequestParam(required = false) String channelCode,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        PageQuery pageQuery = new PageQuery();
        if (page != null) pageQuery.setPage(page);
        if (pageSize != null) pageQuery.setPageSize(pageSize);
        return Result.success(refundOrderService.list(pageQuery, refundNo, orderNo, merchantNo, channelCode, status, startTime, endTime));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取退款详情")
    public Result<RefundOrder> getById(@PathVariable Long id) {
        return Result.success(refundOrderService.getById(id));
    }
}
