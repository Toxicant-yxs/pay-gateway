package com.paygateway.admin.controller;

import com.paygateway.admin.entity.ChannelConfig;
import com.paygateway.admin.entity.ChannelRoute;
import com.paygateway.admin.service.ChannelRouteService;
import com.paygateway.admin.service.ChannelService;
import com.paygateway.common.dto.PageQuery;
import com.paygateway.common.result.PageResult;
import com.paygateway.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/channels")
@RequiredArgsConstructor
@Tag(name = "支付通道接口")
public class ChannelController {

    private final ChannelService channelService;
    private final ChannelRouteService channelRouteService;

    @GetMapping
    @Operation(summary = "查询通道列表")
    public Result<List<ChannelConfig>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        List<ChannelConfig> list = channelService.list(keyword, status);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询通道详情")
    public Result<ChannelConfig> getById(@PathVariable Long id) {
        ChannelConfig channel = channelService.getById(id);
        return Result.success(channel);
    }

    @PostMapping
    @Operation(summary = "创建通道")
    public Result<ChannelConfig> create(@RequestBody ChannelConfig channel) {
        return Result.success(channelService.create(channel));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新通道")
    public Result<ChannelConfig> update(@PathVariable Long id, @RequestBody ChannelConfig channel) {
        return Result.success(channelService.update(id, channel));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新通道状态")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        channelService.updateStatus(id, request.getStatus());
        return Result.success();
    }

    @GetMapping("/routes")
    @Operation(summary = "查询路由规则列表")
    public Result<PageResult<ChannelRoute>> listRoutes(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Integer status) {
        PageQuery pageQuery = new PageQuery();
        if (page != null) pageQuery.setPage(page);
        if (pageSize != null) pageQuery.setPageSize(pageSize);
        return Result.success(channelRouteService.list(pageQuery, status));
    }

    @PostMapping("/routes")
    @Operation(summary = "创建路由规则")
    public Result<ChannelRoute> createRoute(@RequestBody ChannelRoute route) {
        return Result.success(channelRouteService.create(route));
    }

    @PutMapping("/routes/{id}")
    @Operation(summary = "更新路由规则")
    public Result<ChannelRoute> updateRoute(@PathVariable Long id, @RequestBody ChannelRoute route) {
        return Result.success(channelRouteService.update(id, route));
    }

    @DeleteMapping("/routes/{id}")
    @Operation(summary = "删除路由规则")
    public Result<Void> deleteRoute(@PathVariable Long id) {
        channelRouteService.delete(id);
        return Result.success();
    }

    @Data
    public static class StatusRequest {
        private Integer status;
    }
}
