package com.paygateway.admin.interceptor;

import com.paygateway.admin.common.UserContext;
import com.paygateway.common.dto.LoginUser;
import com.paygateway.common.exception.BusinessException;
import com.paygateway.common.result.ResultCode;
import com.paygateway.common.util.JwtUtil;
import com.paygateway.common.util.RequestUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.time.LocalDateTime;

@Slf4j
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

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
            LoginUser loginUser = new LoginUser();
            loginUser.setUserId(Long.parseLong(claims.getSubject()));
            loginUser.setUsername(claims.get("username", String.class));
            loginUser.setRealName(claims.get("realName", String.class));
            loginUser.setRoleCode(claims.get("roleCode", String.class));
            loginUser.setLoginTime(LocalDateTime.now());
            UserContext.setUser(loginUser);
        } catch (Exception e) {
            log.error("Token解析失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
