package com.paygateway.admin.dto;

import lombok.Data;

import java.util.List;

@Data
public class AlertListResponse {

    private List<AlertItem> list;

    private Long total;
}
