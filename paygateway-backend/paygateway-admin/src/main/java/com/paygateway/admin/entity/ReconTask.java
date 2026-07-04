package com.paygateway.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("recon_task")
public class ReconTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("task_no")
    private String taskNo;

    @TableField("channel_code")
    private String channelCode;

    @TableField("recon_date")
    private LocalDate reconDate;

    @TableField("status")
    private Integer status;

    @TableField("total_count")
    private Integer totalCount;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("match_count")
    private Integer matchCount;

    @TableField("diff_count")
    private Integer diffCount;

    @TableField("channel_total_count")
    private Integer channelTotalCount;

    @TableField("channel_total_amount")
    private BigDecimal channelTotalAmount;

    @TableField("bill_file")
    private String billFile;

    @TableField("started_at")
    private LocalDateTime startedAt;

    @TableField("finished_at")
    private LocalDateTime finishedAt;

    @TableField("error_msg")
    private String errorMsg;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("deleted")
    private Integer deleted;
}
