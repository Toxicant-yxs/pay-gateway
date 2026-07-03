package com.paygateway.admin.controller;

import com.paygateway.admin.annotation.RequiresPermissions;
import com.paygateway.admin.entity.SysUser;
import com.paygateway.admin.service.SysUserService;
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
@RequestMapping("/system/users")
@RequiredArgsConstructor
@Tag(name = "用户管理接口")
public class SysUserController {

    private final SysUserService sysUserService;

    @GetMapping
    @Operation(summary = "分页查询用户列表")
    @RequiresPermissions("system:user:list")
    public Result<PageResult<SysUser>> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        PageQuery pageQuery = new PageQuery();
        if (page != null) pageQuery.setPage(page);
        if (pageSize != null) pageQuery.setPageSize(pageSize);
        return Result.success(sysUserService.list(pageQuery, keyword, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    @RequiresPermissions("system:user:query")
    public Result<SysUser> getById(@PathVariable Long id) {
        return Result.success(sysUserService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建用户")
    @RequiresPermissions("system:user:add")
    public Result<SysUser> create(@RequestBody SysUser user) {
        return Result.success(sysUserService.create(user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    @RequiresPermissions("system:user:edit")
    public Result<SysUser> update(@PathVariable Long id, @RequestBody SysUser user) {
        return Result.success(sysUserService.update(id, user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    @RequiresPermissions("system:user:remove")
    public Result<Void> delete(@PathVariable Long id) {
        sysUserService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新用户状态")
    @RequiresPermissions("system:user:edit")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        sysUserService.updateStatus(id, request.getStatus());
        return Result.success();
    }

    @PutMapping("/{id}/reset-password")
    @Operation(summary = "重置密码")
    @RequiresPermissions("system:user:resetPwd")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody PasswordRequest request) {
        sysUserService.resetPassword(id, request.getNewPassword());
        return Result.success();
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "分配用户角色")
    @RequiresPermissions("system:user:edit")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody RoleRequest request) {
        sysUserService.assignRoles(id, request.getRoleIds());
        return Result.success();
    }

    @Data
    public static class StatusRequest {
        private Integer status;
    }

    @Data
    public static class PasswordRequest {
        private String newPassword;
    }

    @Data
    public static class RoleRequest {
        private List<Long> roleIds;
    }
}
