package com.paygateway.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paygateway.admin.dto.DashboardStats;
import com.paygateway.admin.entity.ChannelConfig;
import com.paygateway.admin.entity.MerchantInfo;
import com.paygateway.admin.entity.RefundOrder;
import com.paygateway.admin.entity.TradeOrder;
import com.paygateway.admin.mapper.ChannelConfigMapper;
import com.paygateway.admin.mapper.MerchantInfoMapper;
import com.paygateway.admin.mapper.RefundOrderMapper;
import com.paygateway.admin.mapper.TradeOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final MerchantInfoMapper merchantInfoMapper;
    private final TradeOrderMapper tradeOrderMapper;
    private final RefundOrderMapper refundOrderMapper;
    private final ChannelConfigMapper channelConfigMapper;

    public DashboardStats getStats() {
        DashboardStats stats = new DashboardStats();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        Long todayCount = tradeOrderMapper.selectCount(
                new LambdaQueryWrapper<TradeOrder>()
                        .eq(TradeOrder::getDeleted, 0)
                        .eq(TradeOrder::getStatus, 2)
                        .between(TradeOrder::getPaidAt, todayStart, todayEnd)
        );
        stats.setTodayCount(todayCount);

        List<TradeOrder> todayPaidOrders = tradeOrderMapper.selectList(
                new LambdaQueryWrapper<TradeOrder>()
                        .select(TradeOrder::getActualAmount)
                        .eq(TradeOrder::getDeleted, 0)
                        .eq(TradeOrder::getStatus, 2)
                        .between(TradeOrder::getPaidAt, todayStart, todayEnd)
        );
        BigDecimal todayAmount = todayPaidOrders.stream()
                .map(TradeOrder::getActualAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTodayAmount(todayAmount);

        Long todayRefundCount = refundOrderMapper.selectCount(
                new LambdaQueryWrapper<RefundOrder>()
                        .eq(RefundOrder::getDeleted, 0)
                        .eq(RefundOrder::getStatus, 1)
                        .between(RefundOrder::getRefundedAt, todayStart, todayEnd)
        );
        stats.setTodayRefundCount(todayRefundCount);

        List<RefundOrder> todayRefunds = refundOrderMapper.selectList(
                new LambdaQueryWrapper<RefundOrder>()
                        .select(RefundOrder::getRefundAmount)
                        .eq(RefundOrder::getDeleted, 0)
                        .eq(RefundOrder::getStatus, 1)
                        .between(RefundOrder::getRefundedAt, todayStart, todayEnd)
        );
        BigDecimal todayRefundAmount = todayRefunds.stream()
                .map(RefundOrder::getRefundAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTodayRefundAmount(todayRefundAmount);

        Long totalMerchant = merchantInfoMapper.selectCount(
                new LambdaQueryWrapper<MerchantInfo>()
                        .eq(MerchantInfo::getDeleted, 0)
                        .eq(MerchantInfo::getStatus, 1)
        );
        stats.setTotalMerchant(totalMerchant);

        Long totalOrder = tradeOrderMapper.selectCount(
                new LambdaQueryWrapper<TradeOrder>()
                        .eq(TradeOrder::getDeleted, 0)
        );
        stats.setTotalOrder(totalOrder);

        Long successOrderCount = tradeOrderMapper.selectCount(
                new LambdaQueryWrapper<TradeOrder>()
                        .eq(TradeOrder::getDeleted, 0)
                        .eq(TradeOrder::getStatus, 2)
        );
        if (totalOrder > 0) {
            stats.setSuccessRate(
                    new BigDecimal(successOrderCount)
                            .multiply(new BigDecimal("100"))
                            .divide(new BigDecimal(totalOrder), 2, RoundingMode.HALF_UP)
            );
        } else {
            stats.setSuccessRate(BigDecimal.ZERO);
        }

        List<ChannelConfig> activeChannels = channelConfigMapper.selectList(
                new LambdaQueryWrapper<ChannelConfig>()
                        .select(ChannelConfig::getAvgLatency)
                        .eq(ChannelConfig::getDeleted, 0)
                        .eq(ChannelConfig::getStatus, 1)
        );
        long avgLatency = 200L;
        if (!activeChannels.isEmpty()) {
            avgLatency = (long) activeChannels.stream()
                    .map(ChannelConfig::getAvgLatency)
                    .filter(l -> l != null)
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(200.0);
        }
        stats.setAvgLatency(avgLatency);

        return stats;
    }
}
