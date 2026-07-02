package com.paygateway.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paygateway.admin.entity.ChannelConfig;
import com.paygateway.admin.entity.MerchantInfo;
import com.paygateway.admin.entity.RefundOrder;
import com.paygateway.admin.mapper.ChannelConfigMapper;
import com.paygateway.admin.mapper.MerchantInfoMapper;
import com.paygateway.admin.mapper.RefundOrderMapper;
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
public class RefundOrderService {

    private final RefundOrderMapper refundOrderMapper;
    private final MerchantInfoMapper merchantInfoMapper;
    private final ChannelConfigMapper channelConfigMapper;

    public PageResult<RefundOrder> list(PageQuery pageQuery, String refundNo, String orderNo, String merchantNo,
                                         String merchantName, String channelCode, Integer status, LocalDateTime startTime, LocalDateTime endTime) {
        Page<RefundOrder> page = new Page<>(pageQuery.getPage(), pageQuery.getPageSize());
        LambdaQueryWrapper<RefundOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RefundOrder::getDeleted, 0);
        if (StringUtils.hasText(refundNo)) {
            wrapper.like(RefundOrder::getRefundNo, refundNo);
        }
        if (StringUtils.hasText(orderNo)) {
            wrapper.like(RefundOrder::getOrderNo, orderNo);
        }
        if (StringUtils.hasText(merchantNo)) {
            wrapper.eq(RefundOrder::getMerchantNo, merchantNo);
        }
        if (StringUtils.hasText(merchantName)) {
            List<MerchantInfo> merchants = merchantInfoMapper.selectList(
                    new LambdaQueryWrapper<MerchantInfo>()
                            .select(MerchantInfo::getMerchantNo)
                            .like(MerchantInfo::getMerchantName, merchantName)
            );
            if (!merchants.isEmpty()) {
                wrapper.in(RefundOrder::getMerchantNo,
                        merchants.stream().map(MerchantInfo::getMerchantNo).collect(Collectors.toList()));
            } else {
                wrapper.eq(RefundOrder::getId, -1);
            }
        }
        if (StringUtils.hasText(channelCode)) {
            wrapper.eq(RefundOrder::getChannelCode, channelCode.toUpperCase());
        }
        if (status != null) {
            wrapper.eq(RefundOrder::getStatus, status);
        }
        if (startTime != null) {
            wrapper.ge(RefundOrder::getCreatedAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(RefundOrder::getCreatedAt, endTime);
        }
        wrapper.orderByDesc(RefundOrder::getCreatedAt);
        Page<RefundOrder> result = refundOrderMapper.selectPage(page, wrapper);
        enrichOrders(result.getRecords());
        return PageResult.of(result.getRecords(), result.getTotal(), pageQuery.getPage(), pageQuery.getPageSize());
    }

    public RefundOrder getById(Long id) {
        RefundOrder refund = refundOrderMapper.selectOne(
                new LambdaQueryWrapper<RefundOrder>()
                        .eq(RefundOrder::getId, id)
                        .eq(RefundOrder::getDeleted, 0)
        );
        if (refund == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        enrichOrders(Collections.singletonList(refund));
        return refund;
    }

    private void enrichOrders(List<RefundOrder> orders) {
        if (orders == null || orders.isEmpty()) return;

        Set<Long> merchantIds = orders.stream()
                .map(RefundOrder::getMerchantId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<String> channelCodes = orders.stream()
                .map(RefundOrder::getChannelCode)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        Map<Long, String> merchantNameMap = new HashMap<>();
        Map<String, String> channelNameMap = new HashMap<>();

        if (!merchantIds.isEmpty()) {
            List<MerchantInfo> merchants = merchantInfoMapper.selectList(
                    new LambdaQueryWrapper<MerchantInfo>()
                            .select(MerchantInfo::getId, MerchantInfo::getMerchantName)
                            .in(MerchantInfo::getId, merchantIds)
            );
            merchantNameMap = merchants.stream()
                    .collect(Collectors.toMap(MerchantInfo::getId, MerchantInfo::getMerchantName, (a, b) -> a));
        }

        if (!channelCodes.isEmpty()) {
            List<ChannelConfig> channels = channelConfigMapper.selectList(
                    new LambdaQueryWrapper<ChannelConfig>()
                            .select(ChannelConfig::getChannelCode, ChannelConfig::getChannelName)
                            .in(ChannelConfig::getChannelCode, channelCodes)
            );
            channelNameMap = channels.stream()
                    .collect(Collectors.toMap(ChannelConfig::getChannelCode, ChannelConfig::getChannelName, (a, b) -> a));
        }

        for (RefundOrder order : orders) {
            if (order.getMerchantId() != null) {
                order.setMerchantName(merchantNameMap.getOrDefault(order.getMerchantId(), "-"));
            }
            if (StringUtils.hasText(order.getChannelCode())) {
                order.setChannelName(channelNameMap.getOrDefault(order.getChannelCode(), order.getChannelCode()));
            }
        }
    }
}
