package com.paygateway.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("risk_event")
public class RiskEvent {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("event_no")
    private String eventNo;

    @TableField("category")
    private String category;

    @TableField("risk_level")
    private String riskLevel;

    @TableField("order_no")
    private String orderNo;

    @TableField("merchant_no")
    private String merchantNo;

    @TableField("trigger_rule")
    private String triggerRule;

    @TableField("event_detail")
    private String eventDetail;

    @TableField("status")
    private Integer status;

    @TableField("handle_note")
    private String handleNote;

    @TableField("triggered_at")
    private LocalDateTime triggeredAt;

    @TableField("handled_at")
    private LocalDateTime handledAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("deleted")
    private Integer deleted;
}
