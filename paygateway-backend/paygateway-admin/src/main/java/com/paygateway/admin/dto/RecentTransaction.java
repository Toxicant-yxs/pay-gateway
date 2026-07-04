package com.paygateway.admin.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RecentTransaction {

    private String orderNo;

    private String merchantName;

    private String channelCode;

    private BigDecimal amount;

    private String payType;

    private Integer status;

    private LocalDateTime createdAt;
}
