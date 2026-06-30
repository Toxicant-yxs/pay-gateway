package com.paygateway.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paygateway.admin.entity.TradeOrder;
import com.paygateway.admin.mapper.TradeOrderMapper;
import com.paygateway.common.dto.PageQuery;
import com.paygateway.common.exception.BusinessException;
import com.paygateway.common.result.PageResult;
import com.paygateway.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TradeOrderService {

    private final TradeOrderMapper tradeOrderMapper;

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
            wrapper.eq(TradeOrder::getChannelCode, channelCode);
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
        return order;
    }
}
