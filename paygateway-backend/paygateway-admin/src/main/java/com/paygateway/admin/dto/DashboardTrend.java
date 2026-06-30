package com.paygateway.admin.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardTrend {

    private List<String> xAxis;

    private List<BigDecimal> successAmount;

    private List<Long> successCount;

    private List<Long> failCount;
}
