package com.paygateway.admin.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardStats {

    private BigDecimal todayAmount;

    private Long todayCount;

    private BigDecimal todayRefundAmount;

    private Long todayRefundCount;

    private Long totalMerchant;

    private Long totalOrder;

    private BigDecimal successRate;

    private Long avgLatency;
}
