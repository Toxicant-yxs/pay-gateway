package com.paygateway.admin.dto;

import com.paygateway.common.dto.LoginUser;
import lombok.Data;

@Data
public class LoginResponse {

    private String token;

    private String tokenType = "Bearer";

    private Long expiresIn;

    private LoginUser userInfo;
}
