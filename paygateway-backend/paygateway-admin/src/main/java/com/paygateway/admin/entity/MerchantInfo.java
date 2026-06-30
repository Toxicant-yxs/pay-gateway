package com.paygateway.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("merchant_info")
public class MerchantInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("merchant_no")
    private String merchantNo;

    @TableField("merchant_name")
    private String merchantName;

    @TableField("short_name")
    private String shortName;

    @TableField("industry")
    private String industry;

    @TableField("business_license")
    private String businessLicense;

    @TableField("legal_person")
    private String legalPerson;

    @TableField("contact_name")
    private String contactName;

    @TableField("contact_phone")
    private String contactPhone;

    @TableField("contact_email")
    private String contactEmail;

    @TableField("status")
    private Integer status;

    @TableField("level")
    private Integer level;

    @TableField("auth_status")
    private Integer authStatus;

    @TableField("settle_cycle")
    private String settleCycle;

    @TableField("risk_level")
    private String riskLevel;

    @TableField("daily_limit")
    private BigDecimal dailyLimit;

    @TableField("single_limit")
    private BigDecimal singleLimit;

    @TableField("fee_rate")
    private BigDecimal feeRate;

    @TableField("remark")
    private String remark;

    @TableField("audit_remark")
    private String auditRemark;

    @TableField("audit_time")
    private LocalDateTime auditTime;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("deleted")
    private Integer deleted;
}
