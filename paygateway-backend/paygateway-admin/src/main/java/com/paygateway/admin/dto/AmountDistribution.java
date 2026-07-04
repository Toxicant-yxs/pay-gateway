package com.paygateway.admin.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AmountDistribution {

    private String name;

    private BigDecimal value;

    private BigDecimal amount;

    private BigDecimal percentage;
}
