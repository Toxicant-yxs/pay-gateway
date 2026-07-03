package com.paygateway.admin.service;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paygateway.admin.entity.SysRole;
import com.paygateway.admin.entity.SysUser;
import com.paygateway.admin.entity.SysUserRole;
import com.paygateway.admin.mapper.SysRoleMapper;
import com.paygateway.admin.mapper.SysUserMapper;
import com.paygateway.admin.mapper.SysUserRoleMapper;
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
public class SysUserService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;

    public PageResult<SysUser> list(PageQuery pageQuery, String keyword, Integer status) {
        Page<SysUser> page = new Page<>(pageQuery.getPage(), pageQuery.getPageSize());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, 0)
                .orderByDesc(SysUser::getCreatedAt);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword)
                    .or().like(SysUser::getPhone, keyword)
                    .or().like(SysUser::getEmail, keyword));
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        Page<SysUser> result = sysUserMapper.selectPage(page, wrapper);
        enrichUsers(result.getRecords());
        return PageResult.of(result.getRecords(), result.getTotal(), pageQuery.getPage(), pageQuery.getPageSize());
    }

    public SysUser getById(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        enrichUsers(List.of(user));
        List<SysUserRole> urs = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id)
        );
        user.setRoleIds(urs.stream().map(SysUserRole::getRoleId).collect(Collectors.toList()));
        return user;
    }

    @Transactional
    public SysUser create(SysUser user) {
        Long count = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, user.getUsername())
                        .eq(SysUser::getDeleted, 0)
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户名已存在");
        }
        user.setPassword(BCrypt.hashpw(user.getPassword() != null ? user.getPassword() : "123456"));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeleted(0);
        if (user.getStatus() == null) user.setStatus(1);
        sysUserMapper.insert(user);
        saveUserRoles(user.getId(), user.getRoleIds());
        return getById(user.getId());
    }

    @Transactional
    public SysUser update(Long id, SysUser user) {
        SysUser exist = getById(id);
        exist.setRealName(user.getRealName());
        exist.setEmail(user.getEmail());
        exist.setPhone(user.getPhone());
        exist.setAvatar(user.getAvatar());
        exist.setStatus(user.getStatus());
        if (StringUtils.hasText(user.getPassword())) {
            exist.setPassword(BCrypt.hashpw(user.getPassword()));
        }
        exist.setUpdatedAt(LocalDateTime.now());
        sysUserMapper.updateById(exist);
        saveUserRoles(id, user.getRoleIds());
        return getById(id);
    }

    @Transactional
    public void delete(Long id) {
        SysUser user = getById(id);
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "管理员账号不可删除");
        }
        user.setDeleted(1);
        user.setUpdatedAt(LocalDateTime.now());
        sysUserMapper.updateById(user);
        sysUserRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id)
        );
    }

    public void updateStatus(Long id, Integer status) {
        SysUser user = getById(id);
        if ("admin".equals(user.getUsername()) && status != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "管理员账号不可禁用");
        }
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        sysUserMapper.updateById(user);
    }

    @Transactional
    public void resetPassword(Long id, String newPassword) {
        SysUser user = getById(id);
        user.setPassword(BCrypt.hashpw(StringUtils.hasText(newPassword) ? newPassword : "123456"));
        user.setUpdatedAt(LocalDateTime.now());
        sysUserMapper.updateById(user);
    }

    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        getById(userId);
        saveUserRoles(userId, roleIds);
    }

    private void saveUserRoles(Long userId, List<Long> roleIds) {
        sysUserRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        );
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long rid : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(rid);
                ur.setCreatedAt(LocalDateTime.now());
                sysUserRoleMapper.insert(ur);
            }
        }
    }

    private void enrichUsers(List<SysUser> users) {
        if (users == null || users.isEmpty()) return;
        List<Long> userIds = users.stream().map(SysUser::getId).collect(Collectors.toList());
        List<SysUserRole> urs = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, userIds)
        );
        List<Long> allRoleIds = urs.stream().map(SysUserRole::getRoleId).distinct().collect(Collectors.toList());
        java.util.Map<Long, String> roleNameMap = new java.util.HashMap<>();
        if (!allRoleIds.isEmpty()) {
            List<SysRole> roles = sysRoleMapper.selectList(
                    new LambdaQueryWrapper<SysRole>().in(SysRole::getId, allRoleIds)
            );
            roleNameMap = roles.stream().collect(Collectors.toMap(SysRole::getId, SysRole::getRoleName, (a, b) -> a));
        }
        for (SysUser user : users) {
            List<Long> roleIds = urs.stream()
                    .filter(ur -> ur.getUserId().equals(user.getId()))
                    .map(SysUserRole::getRoleId)
                    .collect(Collectors.toList());
            user.setRoleIds(roleIds);
            if (!roleIds.isEmpty()) {
                user.setRoleName(roleIds.stream().map(roleNameMap::get)
                        .filter(n -> n != null).collect(Collectors.joining(",")));
            }
        }
    }
}
