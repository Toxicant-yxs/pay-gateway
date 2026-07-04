package com.paygateway.admin.controller;

import com.paygateway.admin.annotation.RequiresPermissions;
import com.paygateway.admin.entity.SysPermission;
import com.paygateway.admin.entity.SysRole;
import com.paygateway.admin.service.SysPermissionService;
import com.paygateway.admin.service.SysRoleService;
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
@RequestMapping("/system/roles")
@RequiredArgsConstructor
@Tag(name = "角色管理接口")
public class SysRoleController {

    private final SysRoleService sysRoleService;
    private final SysPermissionService sysPermissionService;

    @GetMapping
    @Operation(summary = "分页查询角色列表")
    @RequiresPermissions("system:role:list")
    public Result<PageResult<SysRole>> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        PageQuery pageQuery = new PageQuery();
        if (page != null) pageQuery.setPage(page);
        if (pageSize != null) pageQuery.setPageSize(pageSize);
        return Result.success(sysRoleService.list(pageQuery, keyword, status));
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有可用角色")
    public Result<List<SysRole>> listAll() {
        return Result.success(sysRoleService.listAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取角色详情")
    @RequiresPermissions("system:role:query")
    public Result<SysRole> getById(@PathVariable Long id) {
        return Result.success(sysRoleService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建角色")
    @RequiresPermissions("system:role:add")
    public Result<SysRole> create(@RequestBody SysRole role) {
        return Result.success(sysRoleService.create(role));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新角色")
    @RequiresPermissions("system:role:edit")
    public Result<SysRole> update(@PathVariable Long id, @RequestBody SysRole role) {
        return Result.success(sysRoleService.update(id, role));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    @RequiresPermissions("system:role:remove")
    public Result<Void> delete(@PathVariable Long id) {
        sysRoleService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/permissions")
    @Operation(summary = "分配角色权限")
    @RequiresPermissions("system:role:edit")
    public Result<Void> assignPermissions(@PathVariable Long id, @RequestBody PermissionRequest request) {
        sysRoleService.assignPermissions(id, request.getPermissionIds());
        return Result.success();
    }

    @GetMapping("/{id}/permissions")
    @Operation(summary = "获取角色已分配的权限ID列表")
    @RequiresPermissions("system:role:query")
    public Result<List<Long>> getRolePermissions(@PathVariable Long id) {
        return Result.success(sysRoleService.getRolePermissionIds(id));
    }

    @GetMapping("/permissions/tree")
    @Operation(summary = "获取权限树")
    public Result<List<SysPermission>> getPermissionTree() {
        return Result.success(sysPermissionService.listAll());
    }

    @Data
    public static class PermissionRequest {
        private List<Long> permissionIds;
    }
}
