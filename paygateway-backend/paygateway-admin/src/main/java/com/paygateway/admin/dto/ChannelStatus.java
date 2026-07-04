package com.paygateway.admin.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ChannelStatus {

    private Long channelId;

    private String channelName;

    private String channelCode;

    private List<String> payTypes;

    private String status;

    private BigDecimal successRate;

    private Long avgLatency;

    private Integer qps;
}
