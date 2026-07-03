package com.paygateway.common.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
    private String realName;
    private String avatar;
    private String roleCode;
    private LocalDateTime loginTime;
    private String email;
    private String phone;
    private String department;
    private List<String> roles;
    private List<String> permissions;

    public boolean isAdmin() {
        return "admin".equals(roleCode) || (roles != null && roles.contains("admin"));
    }

    public boolean hasPermission(String permission) {
        if (isAdmin()) return true;
        return permissions != null && permissions.contains(permission);
    }
}

