package com.paygateway.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("refund_order")
public class RefundOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("refund_no")
    private String refundNo;

    @TableField("order_no")
    private String orderNo;

    @TableField("merchant_id")
    private Long merchantId;

    @TableField("merchant_no")
    private String merchantNo;

    @TableField("channel_code")
    private String channelCode;

    @TableField("channel_refund_no")
    private String channelRefundNo;

    @TableField("refund_amount")
    private BigDecimal refundAmount;

    @TableField("refund_fee")
    private BigDecimal refundFee;

    @TableField("refund_reason")
    private String refundReason;

    @TableField("status")
    private Integer status;

    @TableField("channel_response")
    private String channelResponse;

    @TableField("refunded_at")
    private LocalDateTime refundedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("deleted")
    private Integer deleted;
}
