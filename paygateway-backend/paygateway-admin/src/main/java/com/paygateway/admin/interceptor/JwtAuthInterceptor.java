package com.paygateway.admin.interceptor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paygateway.admin.common.UserContext;
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
import com.paygateway.common.util.RequestUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = RequestUtil.getTokenFromRequest(request);
        if (token == null || token.isEmpty()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        if (!JwtUtil.validateToken(token)) {
            throw new BusinessException(ResultCode.TOKEN_EXPIRED);
        }

        try {
            Claims claims = JwtUtil.parseToken(token);
            Long userId = Long.parseLong(claims.getSubject());

            SysUser user = sysUserMapper.selectById(userId);
            if (user == null || user.getDeleted() == 1) {
                throw new BusinessException(ResultCode.USER_NOT_EXIST);
            }
            if (user.getStatus() != 1) {
                throw new BusinessException(ResultCode.USER_DISABLED);
            }

            LoginUser loginUser = new LoginUser();
            loginUser.setUserId(userId);
            loginUser.setUsername(user.getUsername());
            loginUser.setRealName(user.getRealName());
            loginUser.setAvatar(user.getAvatar());
            loginUser.setEmail(user.getEmail());
            loginUser.setPhone(user.getPhone());
            loginUser.setRoleCode(user.getRoleCode());
            loginUser.setLoginTime(LocalDateTime.now());

            loadUserRolesAndPermissions(loginUser, userId);

            UserContext.setUser(loginUser);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Token解析失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        return true;
    }

    private void loadUserRolesAndPermissions(LoginUser loginUser, Long userId) {
        List<String> roleCodes = new ArrayList<>();
        List<String> roleNames = new ArrayList<>();

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
                if (StringUtils.hasText(role.getRoleName())) {
                    roleNames.add(role.getRoleName());
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

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
