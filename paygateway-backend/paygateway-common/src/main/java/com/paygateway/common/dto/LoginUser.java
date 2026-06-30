package com.paygateway.common.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
    private String realName;
    private String avatar;
    private String roleCode;
    private LocalDateTime loginTime;
}
