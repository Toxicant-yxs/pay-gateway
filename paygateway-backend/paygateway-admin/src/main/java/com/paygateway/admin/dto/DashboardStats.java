package com.paygateway.admin.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardStats {

    private BigDecimal todayAmount;

    private BigDecimal todayAmountGrowth;

    private Long todayCount;

    private BigDecimal todayCountGrowth;

    private BigDecimal todayRefundAmount;

    private Long todayRefundCount;

    private Long totalMerchant;

    private Long totalOrder;

    private BigDecimal successRate;

    private BigDecimal successRateChange;

    private Long avgLatency;

    private BigDecimal avgResponseTimeChange;
}
