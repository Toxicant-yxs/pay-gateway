package com.paygateway.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paygateway.admin.entity.ChannelConfig;
import com.paygateway.admin.entity.MerchantInfo;
import com.paygateway.admin.entity.TradeOrder;
import com.paygateway.admin.mapper.ChannelConfigMapper;
import com.paygateway.admin.mapper.MerchantInfoMapper;
import com.paygateway.admin.mapper.TradeOrderMapper;
import com.paygateway.common.dto.PageQuery;
import com.paygateway.common.exception.BusinessException;
import com.paygateway.common.result.PageResult;
import com.paygateway.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradeOrderService {

    private final TradeOrderMapper tradeOrderMapper;
    private final MerchantInfoMapper merchantInfoMapper;
    private final ChannelConfigMapper channelConfigMapper;

    public PageResult<TradeOrder> list(PageQuery pageQuery, String orderNo, String merchantNo,
                                       String channelCode, Integer status,
                                       LocalDateTime startTime, LocalDateTime endTime) {
        Page<TradeOrder> page = new Page<>(pageQuery.getPage(), pageQuery.getPageSize());

        LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<TradeOrder>()
                .eq(TradeOrder::getDeleted, 0)
                .orderByDesc(TradeOrder::getCreatedAt);

        if (StringUtils.hasText(orderNo)) {
            wrapper.like(TradeOrder::getOrderNo, orderNo);
        }
        if (StringUtils.hasText(merchantNo)) {
            wrapper.eq(TradeOrder::getMerchantNo, merchantNo);
        }
        if (StringUtils.hasText(channelCode)) {
            wrapper.eq(TradeOrder::getChannelCode, channelCode.toUpperCase());
        }
        if (status != null) {
            wrapper.eq(TradeOrder::getStatus, status);
        }
        if (startTime != null) {
            wrapper.ge(TradeOrder::getCreatedAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(TradeOrder::getCreatedAt, endTime);
        }

        Page<TradeOrder> result = tradeOrderMapper.selectPage(page, wrapper);
        enrichOrders(result.getRecords());
        return PageResult.of(result.getRecords(), result.getTotal(), pageQuery.getPage(), pageQuery.getPageSize());
    }

    public TradeOrder getById(Long id) {
        TradeOrder order = tradeOrderMapper.selectOne(
                new LambdaQueryWrapper<TradeOrder>()
                        .eq(TradeOrder::getId, id)
                        .eq(TradeOrder::getDeleted, 0)
        );
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        enrichOrders(Collections.singletonList(order));
        return order;
    }

    private void enrichOrders(List<TradeOrder> orders) {
        if (orders == null || orders.isEmpty()) return;

        Set<Long> merchantIds = orders.stream()
                .map(TradeOrder::getMerchantId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> channelIds = orders.stream()
                .map(TradeOrder::getChannelId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> merchantNameMap = new HashMap<>();
        Map<Long, String> channelNameMap = new HashMap<>();

        if (!merchantIds.isEmpty()) {
            List<MerchantInfo> merchants = merchantInfoMapper.selectList(
                    new LambdaQueryWrapper<MerchantInfo>()
                            .select(MerchantInfo::getId, MerchantInfo::getMerchantName)
                            .in(MerchantInfo::getId, merchantIds)
            );
            merchantNameMap = merchants.stream()
                    .collect(Collectors.toMap(MerchantInfo::getId, MerchantInfo::getMerchantName, (a, b) -> a));
        }

        if (!channelIds.isEmpty()) {
            List<ChannelConfig> channels = channelConfigMapper.selectList(
                    new LambdaQueryWrapper<ChannelConfig>()
                            .select(ChannelConfig::getId, ChannelConfig::getChannelName)
                            .in(ChannelConfig::getId, channelIds)
            );
            channelNameMap = channels.stream()
                    .collect(Collectors.toMap(ChannelConfig::getId, ChannelConfig::getChannelName, (a, b) -> a));
        }

        for (TradeOrder order : orders) {
            if (order.getMerchantId() != null) {
                order.setMerchantName(merchantNameMap.getOrDefault(order.getMerchantId(), "-"));
            }
            if (order.getChannelId() != null) {
                order.setChannelName(channelNameMap.getOrDefault(order.getChannelId(), "-"));
            }
        }
    }
}
