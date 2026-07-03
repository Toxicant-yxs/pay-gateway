package com.paygateway.admin.service;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paygateway.admin.common.UserContext;
import com.paygateway.admin.dto.LoginRequest;
import com.paygateway.admin.dto.LoginResponse;
import com.paygateway.admin.entity.SysRole;
import com.paygateway.admin.entity.SysUser;
import com.paygateway.admin.entity.SysUserRole;
import com.paygateway.admin.mapper.SysPermissionMapper;
import com.paygateway.admin.mapper.SysRoleMapper;
import com.paygateway.admin.mapper.SysUserMapper;
import com.paygateway.admin.mapper.SysUserRoleMapper;
import com.paygateway.common.dto.LoginUser;
import com.paygateway.common.exception.BusinessException;
import com.paygateway.common.result.ResultCode;
import com.paygateway.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;

    private static final long EXPIRES_IN = 24 * 60 * 60L;

    public LoginResponse login(LoginRequest request, String ip) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, request.getUsername())
                        .eq(SysUser::getDeleted, 0)
        );

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        sysUserMapper.updateById(user);

        String token = JwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                Map.of("realName", user.getRealName() != null ? user.getRealName() : "",
                        "roleCode", user.getRoleCode() != null ? user.getRoleCode() : "admin")
        );

        LoginUser loginUser = buildLoginUser(user);

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setTokenType("Bearer");
        response.setExpiresIn(EXPIRES_IN);
        response.setUserInfo(loginUser);

        return response;
    }

    public LoginUser getCurrentUser() {
        LoginUser user = UserContext.getUser();
        if (user == null) {
            return null;
        }
        if (user.getRoles() == null) {
            SysUser sysUser = sysUserMapper.selectById(user.getUserId());
            if (sysUser != null) {
                user = buildLoginUser(sysUser);
                UserContext.setUser(user);
            }
        }
        return user;
    }

    private LoginUser buildLoginUser(SysUser user) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setRealName(user.getRealName());
        loginUser.setAvatar(user.getAvatar());
        loginUser.setEmail(user.getEmail());
        loginUser.setPhone(user.getPhone());
        loginUser.setRoleCode(user.getRoleCode());
        loginUser.setLoginTime(LocalDateTime.now());

        loadUserRolesAndPermissions(loginUser, user.getId());
        return loginUser;
    }

    private void loadUserRolesAndPermissions(LoginUser loginUser, Long userId) {
        List<String> roleCodes = new ArrayList<>();
        if (StringUtils.hasText(loginUser.getRoleCode())) {
            roleCodes.add(loginUser.getRoleCode());
        }

        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        );
        if (!userRoles.isEmpty()) {
            List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
            List<SysRole> roles = sysRoleMapper.selectList(
                    new LambdaQueryWrapper<SysRole>()
                            .in(SysRole::getId, roleIds)
                            .eq(SysRole::getStatus, 1)
                            .eq(SysRole::getDeleted, 0)
            );
            for (SysRole role : roles) {
                if (!roleCodes.contains(role.getRoleCode())) {
                    roleCodes.add(role.getRoleCode());
                }
            }
        }

        loginUser.setRoles(roleCodes);

        if ("admin".equals(loginUser.getRoleCode()) || roleCodes.contains("admin")) {
            loginUser.setPermissions(List.of("*"));
        } else {
            List<String> permissions = sysPermissionMapper.selectPermissionCodesByUserId(userId);
            loginUser.setPermissions(permissions);
        }
    }
}
