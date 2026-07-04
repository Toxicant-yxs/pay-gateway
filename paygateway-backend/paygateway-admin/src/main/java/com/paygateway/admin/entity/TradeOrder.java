package com.paygateway.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("trade_order")
public class TradeOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("order_no")
    private String orderNo;

    @TableField("merchant_order_no")
    private String merchantOrderNo;

    @TableField("merchant_id")
    private Long merchantId;

    @TableField("merchant_no")
    private String merchantNo;

    @TableField("channel_id")
    private Long channelId;

    @TableField("channel_code")
    private String channelCode;

    @TableField("channel_order_no")
    private String channelOrderNo;

    @TableField("pay_type")
    private String payType;

    @TableField("subject")
    private String subject;

    @TableField("body")
    private String body;

    @TableField("amount")
    private BigDecimal amount;

    @TableField("actual_amount")
    private BigDecimal actualAmount;

    @TableField("fee")
    private BigDecimal fee;

    @TableField("fee_rate")
    private BigDecimal feeRate;

    @TableField("currency")
    private String currency;

    @TableField("status")
    private Integer status;

    @TableField("client_ip")
    private String clientIp;

    @TableField("notify_url")
    private String notifyUrl;

    @TableField("return_url")
    private String returnUrl;

    @TableField("expire_time")
    private LocalDateTime expireTime;

    @TableField("paid_at")
    private LocalDateTime paidAt;

    @TableField("channel_response")
    private String channelResponse;

    @TableField("extra")
    private String extra;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("deleted")
    private Integer deleted;

    @TableField(exist = false)
    private String merchantName;

    @TableField(exist = false)
    private String channelName;
}
