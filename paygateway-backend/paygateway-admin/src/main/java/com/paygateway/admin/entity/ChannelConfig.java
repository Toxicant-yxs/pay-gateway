package com.paygateway.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("channel_config")
public class ChannelConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("channel_code")
    private String channelCode;

    @TableField("channel_name")
    private String channelName;

    @TableField("channel_type")
    private String channelType;

    @TableField("pay_types")
    private String payTypes;

    @TableField("app_id")
    private String appId;

    @TableField("mch_id")
    private String mchId;

    @TableField("api_key")
    private String apiKey;

    @TableField("cert_path")
    private String certPath;

    @TableField("api_url")
    private String apiUrl;

    @TableField("fee_rate")
    private BigDecimal feeRate;

    @TableField("remark")
    private String remark;

    @TableField("status")
    private Integer status;

    @TableField("priority")
    private Integer priority;

    @TableField("weight")
    private Integer weight;

    @TableField("avg_success_rate")
    private BigDecimal avgSuccessRate;

    @TableField("avg_latency")
    private Long avgLatency;

    @TableField("current_qps")
    private Integer currentQps;

    @TableField("daily_amount")
    private BigDecimal dailyAmount;

    @TableField("daily_count")
    private Long dailyCount;

    @TableField("config")
    private String config;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("deleted")
    private Integer deleted;
}
