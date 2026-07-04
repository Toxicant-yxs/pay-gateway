package com.paygateway.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("channel_route")
public class ChannelRoute {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("route_no")
    private String routeNo;

    @TableField("route_name")
    private String routeName;

    @TableField("channel_id")
    private Long channelId;

    @TableField("channel_code")
    private String channelCode;

    @TableField("pay_type")
    private String payType;

    @TableField("min_amount")
    private java.math.BigDecimal minAmount;

    @TableField("max_amount")
    private java.math.BigDecimal maxAmount;

    @TableField("priority")
    private Integer priority;

    @TableField("status")
    private Integer status;

    @TableField("time_start")
    private String timeStart;

    @TableField("time_end")
    private String timeEnd;

    @TableField("remark")
    private String remark;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("deleted")
    private Integer deleted;
}
