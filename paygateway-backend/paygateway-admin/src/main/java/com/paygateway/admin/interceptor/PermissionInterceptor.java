package com.paygateway.admin.interceptor;

import com.paygateway.admin.annotation.RequiresPermissions;
import com.paygateway.admin.common.UserContext;
import com.paygateway.common.dto.LoginUser;
import com.paygateway.common.exception.BusinessException;
import com.paygateway.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;
import java.util.Arrays;

@Slf4j
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        Method method = handlerMethod.getMethod();
        Class<?> clazz = method.getDeclaringClass();

        RequiresPermissions methodAnnotation = method.getAnnotation(RequiresPermissions.class);
        RequiresPermissions classAnnotation = clazz.getAnnotation(RequiresPermissions.class);

        if (methodAnnotation == null && classAnnotation == null) {
            return true;
        }

        LoginUser user = UserContext.getUser();
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        if (user.isAdmin()) {
            return true;
        }

        if (classAnnotation != null) {
            checkPermissions(user, classAnnotation);
        }
        if (methodAnnotation != null) {
            checkPermissions(user, methodAnnotation);
        }

        return true;
    }

    private void checkPermissions(LoginUser user, RequiresPermissions annotation) {
        String[] requiredPermissions = annotation.value();
        if (requiredPermissions.length == 0) {
            return;
        }

        if (annotation.logical() == RequiresPermissions.Logical.AND) {
            for (String permission : requiredPermissions) {
                if (!user.hasPermission(permission)) {
                    log.warn("用户[{}]缺少权限: {}", user.getUsername(), permission);
                    throw new BusinessException(ResultCode.FORBIDDEN);
                }
            }
        } else {
            boolean hasAny = false;
            for (String permission : requiredPermissions) {
                if (user.hasPermission(permission)) {
                    hasAny = true;
                    break;
                }
            }
            if (!hasAny) {
                log.warn("用户[{}]缺少任一权限: {}", user.getUsername(), Arrays.toString(requiredPermissions));
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
        }
    }
}
