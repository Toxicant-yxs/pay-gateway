package com.paygateway.admin.service;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paygateway.admin.common.UserContext;
import com.paygateway.admin.dto.LoginRequest;
import com.paygateway.admin.dto.LoginResponse;
import com.paygateway.admin.entity.SysUser;
import com.paygateway.admin.mapper.SysUserMapper;
import com.paygateway.common.dto.LoginUser;
import com.paygateway.common.exception.BusinessException;
import com.paygateway.common.result.ResultCode;
import com.paygateway.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper sysUserMapper;

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

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setRealName(user.getRealName());
        loginUser.setAvatar(user.getAvatar());
        loginUser.setRoleCode(user.getRoleCode());
        loginUser.setLoginTime(LocalDateTime.now());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setTokenType("Bearer");
        response.setExpiresIn(EXPIRES_IN);
        response.setUserInfo(loginUser);

        return response;
    }

    public LoginUser getCurrentUser() {
        return UserContext.getUser();
    }
}
