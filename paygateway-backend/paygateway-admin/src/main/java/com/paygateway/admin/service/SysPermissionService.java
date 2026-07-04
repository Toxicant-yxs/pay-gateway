package com.paygateway.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paygateway.admin.entity.SysPermission;
import com.paygateway.admin.mapper.SysPermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysPermissionService {

    private final SysPermissionMapper sysPermissionMapper;

    public List<SysPermission> list(String keyword, Integer type, Integer status) {
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getDeleted, 0)
                .orderByAsc(SysPermission::getSortOrder);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysPermission::getPermissionName, keyword)
                    .or().like(SysPermission::getPermissionCode, keyword));
        }
        if (type != null) {
            wrapper.eq(SysPermission::getPermissionType, type);
        }
        if (status != null) {
            wrapper.eq(SysPermission::getStatus, status);
        }
        List<SysPermission> list = sysPermissionMapper.selectList(wrapper);
        return buildTree(list);
    }

    public List<SysPermission> listAll() {
        List<SysPermission> list = sysPermissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>()
                        .eq(SysPermission::getDeleted, 0)
                        .eq(SysPermission::getStatus, 1)
                        .orderByAsc(SysPermission::getSortOrder)
        );
        return buildTree(list);
    }

    public SysPermission getById(Long id) {
        SysPermission permission = sysPermissionMapper.selectById(id);
        if (permission == null || permission.getDeleted() == 1) {
            throw new com.paygateway.common.exception.BusinessException(com.paygateway.common.result.ResultCode.NOT_FOUND);
        }
        return permission;
    }

    public SysPermission create(SysPermission permission) {
        Long count = sysPermissionMapper.selectCount(
                new LambdaQueryWrapper<SysPermission>()
                        .eq(SysPermission::getPermissionCode, permission.getPermissionCode())
                        .eq(SysPermission::getDeleted, 0)
        );
        if (count > 0) {
            throw new com.paygateway.common.exception.BusinessException(com.paygateway.common.result.ResultCode.PARAM_ERROR.getCode(), "权限编码已存在");
        }
        permission.setCreatedAt(LocalDateTime.now());
        permission.setUpdatedAt(LocalDateTime.now());
        permission.setDeleted(0);
        if (permission.getStatus() == null) permission.setStatus(1);
        if (permission.getVisible() == null) permission.setVisible(1);
        if (permission.getSortOrder() == null) permission.setSortOrder(0);
        if (permission.getPermissionType() == null) permission.setPermissionType(1);
        if (permission.getParentId() == null) permission.setParentId(0L);
        sysPermissionMapper.insert(permission);
        return permission;
    }

    public SysPermission update(Long id, SysPermission permission) {
        SysPermission exist = getById(id);
        exist.setPermissionName(permission.getPermissionName());
        exist.setPermissionType(permission.getPermissionType());
        exist.setPath(permission.getPath());
        exist.setComponent(permission.getComponent());
        exist.setIcon(permission.getIcon());
        exist.setSortOrder(permission.getSortOrder());
        exist.setVisible(permission.getVisible());
        exist.setStatus(permission.getStatus());
        exist.setRemark(permission.getRemark());
        exist.setParentId(permission.getParentId());
        exist.setUpdatedAt(LocalDateTime.now());
        sysPermissionMapper.updateById(exist);
        return exist;
    }

    public void delete(Long id) {
        SysPermission permission = getById(id);
        Long childCount = sysPermissionMapper.selectCount(
                new LambdaQueryWrapper<SysPermission>()
                        .eq(SysPermission::getParentId, id)
                        .eq(SysPermission::getDeleted, 0)
        );
        if (childCount > 0) {
            throw new com.paygateway.common.exception.BusinessException(com.paygateway.common.result.ResultCode.PARAM_ERROR.getCode(), "存在子权限，无法删除");
        }
        permission.setDeleted(1);
        permission.setUpdatedAt(LocalDateTime.now());
        sysPermissionMapper.updateById(permission);
    }

    private List<SysPermission> buildTree(List<SysPermission> list) {
        Map<Long, SysPermission> map = list.stream().collect(Collectors.toMap(SysPermission::getId, p -> p));
        List<SysPermission> roots = new ArrayList<>();
        for (SysPermission p : list) {
            if (p.getParentId() == null || p.getParentId() == 0) {
                roots.add(p);
            } else {
                SysPermission parent = map.get(p.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) parent.setChildren(new ArrayList<>());
                    parent.getChildren().add(p);
                } else {
                    roots.add(p);
                }
            }
        }
        return roots;
    }
}
