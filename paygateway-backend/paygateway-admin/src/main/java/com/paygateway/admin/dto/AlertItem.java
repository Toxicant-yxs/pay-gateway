package com.paygateway.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AlertItem {

    private Long alertId;

    private String type;

    private String level;

    private String title;

    private String content;

    private LocalDateTime createdAt;

    private Boolean read;
}
