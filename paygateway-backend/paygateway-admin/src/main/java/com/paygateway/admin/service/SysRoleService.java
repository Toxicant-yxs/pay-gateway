package com.paygateway.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paygateway.admin.entity.SysPermission;
import com.paygateway.admin.entity.SysRole;
import com.paygateway.admin.entity.SysRolePermission;
import com.paygateway.admin.mapper.SysPermissionMapper;
import com.paygateway.admin.mapper.SysRoleMapper;
import com.paygateway.admin.mapper.SysRolePermissionMapper;
import com.paygateway.common.dto.PageQuery;
import com.paygateway.common.exception.BusinessException;
import com.paygateway.common.result.PageResult;
import com.paygateway.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysRoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysPermissionMapper sysPermissionMapper;

    public PageResult<SysRole> list(PageQuery pageQuery, String keyword, Integer status) {
        Page<SysRole> page = new Page<>(pageQuery.getPage(), pageQuery.getPageSize());
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getDeleted, 0)
                .orderByAsc(SysRole::getSortOrder);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysRole::getRoleName, keyword).or().like(SysRole::getRoleCode, keyword));
        }
        if (status != null) {
            wrapper.eq(SysRole::getStatus, status);
        }
        Page<SysRole> result = sysRoleMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), pageQuery.getPage(), pageQuery.getPageSize());
    }

    public List<SysRole> listAll() {
        return sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getDeleted, 0)
                        .eq(SysRole::getStatus, 1)
                        .orderByAsc(SysRole::getSortOrder)
        );
    }

    public SysRole getById(Long id) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null || role.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        List<SysRolePermission> rps = sysRolePermissionMapper.selectList(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, id)
        );
        role.setPermissionIds(rps.stream().map(SysRolePermission::getPermissionId).collect(Collectors.toList()));
        return role;
    }

    @Transactional
    public SysRole create(SysRole role) {
        Long count = sysRoleMapper.selectCount(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getRoleCode, role.getRoleCode())
                        .eq(SysRole::getDeleted, 0)
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "角色编码已存在");
        }
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        role.setDeleted(0);
        if (role.getStatus() == null) role.setStatus(1);
        if (role.getDataScope() == null) role.setDataScope(1);
        if (role.getSortOrder() == null) role.setSortOrder(0);
        sysRoleMapper.insert(role);
        saveRolePermissions(role.getId(), role.getPermissionIds());
        return role;
    }

    @Transactional
    public SysRole update(Long id, SysRole role) {
        SysRole exist = getById(id);
        exist.setRoleName(role.getRoleName());
        exist.setDescription(role.getDescription());
        exist.setDataScope(role.getDataScope());
        exist.setStatus(role.getStatus());
        exist.setSortOrder(role.getSortOrder());
        exist.setUpdatedAt(LocalDateTime.now());
        sysRoleMapper.updateById(exist);
        saveRolePermissions(id, role.getPermissionIds());
        return getById(id);
    }

    @Transactional
    public void delete(Long id) {
        SysRole role = getById(id);
        if ("admin".equals(role.getRoleCode())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "管理员角色不可删除");
        }
        role.setDeleted(1);
        role.setUpdatedAt(LocalDateTime.now());
        sysRoleMapper.updateById(role);
        sysRolePermissionMapper.delete(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, id)
        );
    }

    private void saveRolePermissions(Long roleId, List<Long> permissionIds) {
        sysRolePermissionMapper.delete(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId)
        );
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long pid : permissionIds) {
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(pid);
                rp.setCreatedAt(LocalDateTime.now());
                sysRolePermissionMapper.insert(rp);
            }
        }
    }

    public List<Long> getRolePermissionIds(Long roleId) {
        getById(roleId);
        List<SysRolePermission> rps = sysRolePermissionMapper.selectList(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId)
        );
        return rps.stream().map(SysRolePermission::getPermissionId).collect(Collectors.toList());
    }

    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        getById(roleId);
        saveRolePermissions(roleId, permissionIds);
    }
}
