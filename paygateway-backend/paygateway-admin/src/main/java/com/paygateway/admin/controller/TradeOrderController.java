package com.paygateway.admin.controller;

import com.paygateway.admin.entity.TradeOrder;
import com.paygateway.admin.service.TradeOrderService;
import com.paygateway.common.dto.PageQuery;
import com.paygateway.common.result.PageResult;
import com.paygateway.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/trade/orders")
@RequiredArgsConstructor
@Tag(name = "交易订单接口")
public class TradeOrderController {

    private final TradeOrderService tradeOrderService;

    @GetMapping
    @Operation(summary = "分页查询交易订单")
    public Result<PageResult<TradeOrder>> list(
            @Parameter(description = "页码") @RequestParam(required = false) Integer page,
            @Parameter(description = "每页数量") @RequestParam(required = false) Integer pageSize,
            @Parameter(description = "订单号") @RequestParam(required = false) String orderNo,
            @Parameter(description = "商户号") @RequestParam(required = false) String merchantNo,
            @Parameter(description = "通道编码") @RequestParam(required = false) String channelCode,
            @Parameter(description = "订单状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        PageQuery pageQuery = new PageQuery();
        if (page != null) {
            pageQuery.setPage(page);
        }
        if (pageSize != null) {
            pageQuery.setPageSize(pageSize);
        }
        PageResult<TradeOrder> result = tradeOrderService.list(pageQuery, orderNo, merchantNo, channelCode, status, startTime, endTime);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取订单详情")
    public Result<TradeOrder> getById(@PathVariable Long id) {
        TradeOrder order = tradeOrderService.getById(id);
        return Result.success(order);
    }
}
