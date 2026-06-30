package com.paygateway.admin.controller;

import com.paygateway.admin.entity.ChannelConfig;
import com.paygateway.admin.service.ChannelService;
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

    @GetMapping
    @Operation(summary = "查询通道列表")
    public Result<List<ChannelConfig>> list() {
        List<ChannelConfig> list = channelService.list();
        return Result.success(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询通道详情")
    public Result<ChannelConfig> getById(@PathVariable Long id) {
        ChannelConfig channel = channelService.getById(id);
        return Result.success(channel);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新通道状态")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        channelService.updateStatus(id, request.getStatus());
        return Result.success();
    }

    @Data
    public static class StatusRequest {
        private Integer status;
    }
}
