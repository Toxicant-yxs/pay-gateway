package com.paygateway.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paygateway.admin.entity.RefundOrder;
import com.paygateway.admin.mapper.RefundOrderMapper;
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
public class RefundOrderService {

    private final RefundOrderMapper refundOrderMapper;

    public PageResult<RefundOrder> list(PageQuery pageQuery, String refundNo, String orderNo, String merchantNo,
                                         String channelCode, Integer status, LocalDateTime startTime, LocalDateTime endTime) {
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
        if (StringUtils.hasText(channelCode)) {
            wrapper.eq(RefundOrder::getChannelCode, channelCode);
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
        return refund;
    }
}
