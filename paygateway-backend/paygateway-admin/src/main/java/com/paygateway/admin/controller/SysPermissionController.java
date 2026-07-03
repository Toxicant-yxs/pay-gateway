package com.paygateway.admin.controller;

import com.paygateway.admin.entity.SysPermission;
import com.paygateway.admin.service.SysPermissionService;
import com.paygateway.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/permissions")
@RequiredArgsConstructor
@Tag(name = "权限管理接口")
public class SysPermissionController {

    private final SysPermissionService sysPermissionService;

    @GetMapping
    @Operation(summary = "查询权限列表")
    public Result<List<SysPermission>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status) {
        return Result.success(sysPermissionService.list(keyword, type, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取权限详情")
    public Result<SysPermission> getById(@PathVariable Long id) {
        return Result.success(sysPermissionService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建权限")
    public Result<SysPermission> create(@RequestBody SysPermission permission) {
        return Result.success(sysPermissionService.create(permission));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新权限")
    public Result<SysPermission> update(@PathVariable Long id, @RequestBody SysPermission permission) {
        return Result.success(sysPermissionService.update(id, permission));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除权限")
    public Result<Void> delete(@PathVariable Long id) {
        sysPermissionService.delete(id);
        return Result.success();
    }
}
