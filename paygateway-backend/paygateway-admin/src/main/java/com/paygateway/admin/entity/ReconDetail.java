package com.paygateway.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("recon_detail")
public class ReconDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("task_id")
    private Long taskId;

    @TableField("order_no")
    private String orderNo;

    @TableField("diff_type")
    private String diffType;

    @TableField("our_amount")
    private BigDecimal ourAmount;

    @TableField("channel_amount")
    private BigDecimal channelAmount;

    @TableField("our_status")
    private Integer ourStatus;

    @TableField("channel_status")
    private Integer channelStatus;

    @TableField("status")
    private Integer status;

    @TableField("handle_note")
    private String handleNote;

    @TableField("handled_at")
    private LocalDateTime handledAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("deleted")
    private Integer deleted;
}
