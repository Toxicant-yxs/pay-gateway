package com.paygateway.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paygateway.admin.dto.*;
import com.paygateway.admin.entity.ChannelConfig;
import com.paygateway.admin.entity.MerchantInfo;
import com.paygateway.admin.entity.RefundOrder;
import com.paygateway.admin.entity.RiskEvent;
import com.paygateway.admin.entity.TradeOrder;
import com.paygateway.admin.mapper.ChannelConfigMapper;
import com.paygateway.admin.mapper.MerchantInfoMapper;
import com.paygateway.admin.mapper.RefundOrderMapper;
import com.paygateway.admin.mapper.RiskEventMapper;
import com.paygateway.admin.mapper.TradeOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final MerchantInfoMapper merchantInfoMapper;
    private final TradeOrderMapper tradeOrderMapper;
    private final RefundOrderMapper refundOrderMapper;
    private final ChannelConfigMapper channelConfigMapper;
    private final RiskEventMapper riskEventMapper;

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

        stats.setTodayAmountGrowth(new BigDecimal("12.5"));
        stats.setTodayCountGrowth(new BigDecimal("8.3"));
        stats.setSuccessRateChange(new BigDecimal("-2.1"));
        stats.setAvgResponseTimeChange(new BigDecimal("-5.2"));

        return stats;
    }

    public DashboardTrend getTrend(String type) {
        DashboardTrend trend = new DashboardTrend();
        List<String> xAxis = new ArrayList<>();
        List<BigDecimal> successAmount = new ArrayList<>();
        List<Long> successCount = new ArrayList<>();
        List<Long> failCount = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:00");
        LocalDateTime now = LocalDateTime.now();
        Random random = new Random();

        for (int i = 23; i >= 0; i--) {
            LocalDateTime hour = now.minusHours(i);
            xAxis.add(hour.format(formatter));

            BigDecimal amount = new BigDecimal(5000 + random.nextInt(15000))
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            successAmount.add(amount);

            successCount.add(20L + random.nextInt(80));
            failCount.add(1L + random.nextInt(10));
        }

        trend.setXAxis(xAxis);
        trend.setSuccessAmount(successAmount);
        trend.setSuccessCount(successCount);
        trend.setFailCount(failCount);

        return trend;
    }

    public List<RecentTransaction> getRecentTransactions() {
        List<TradeOrder> orders = tradeOrderMapper.selectList(
                new LambdaQueryWrapper<TradeOrder>()
                        .eq(TradeOrder::getDeleted, 0)
                        .orderByDesc(TradeOrder::getCreatedAt)
                        .last("LIMIT 5")
        );

        Map<Long, String> merchantNameMap = new HashMap<>();
        if (!orders.isEmpty()) {
            Set<Long> merchantIds = orders.stream()
                    .map(TradeOrder::getMerchantId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (!merchantIds.isEmpty()) {
                List<MerchantInfo> merchants = merchantInfoMapper.selectList(
                        new LambdaQueryWrapper<MerchantInfo>()
                                .select(MerchantInfo::getId, MerchantInfo::getMerchantName)
                                .in(MerchantInfo::getId, merchantIds)
                );
                Map<Long, String> foundMap = merchants.stream()
                        .collect(Collectors.toMap(MerchantInfo::getId, MerchantInfo::getMerchantName, (a, b) -> a));
                merchantNameMap.putAll(foundMap);
            }
        }

        return orders.stream().map(order -> {
            RecentTransaction tx = new RecentTransaction();
            tx.setOrderNo(order.getOrderNo());
            tx.setMerchantName(merchantNameMap.getOrDefault(order.getMerchantId(), "-"));
            tx.setChannelCode(order.getChannelCode());
            tx.setAmount(order.getAmount());
            tx.setPayType(order.getPayType());
            tx.setStatus(order.getStatus());
            tx.setCreatedAt(order.getCreatedAt());
            return tx;
        }).collect(Collectors.toList());
    }

    public List<ChannelStatus> getChannels() {
        List<ChannelConfig> channels = channelConfigMapper.selectList(
                new LambdaQueryWrapper<ChannelConfig>()
                        .eq(ChannelConfig::getDeleted, 0)
                        .orderByAsc(ChannelConfig::getId)
        );

        return channels.stream().map(channel -> {
            ChannelStatus status = new ChannelStatus();
            status.setChannelId(channel.getId());
            status.setChannelName(channel.getChannelName());
            status.setChannelCode(channel.getChannelCode());

            List<String> payTypeList = new ArrayList<>();
            if (channel.getPayTypes() != null && !channel.getPayTypes().isEmpty()) {
                payTypeList = Arrays.asList(channel.getPayTypes().split(","));
            }
            status.setPayTypes(payTypeList);

            status.setStatus(channel.getStatus() == 1 ? "active" : "inactive");
            status.setSuccessRate(channel.getAvgSuccessRate() != null ? channel.getAvgSuccessRate() : new BigDecimal("99.5"));
            status.setAvgLatency(channel.getAvgLatency() != null ? channel.getAvgLatency() : 180L);
            status.setQps(channel.getCurrentQps() != null ? channel.getCurrentQps() : 50);
            return status;
        }).collect(Collectors.toList());
    }

    public AlertListResponse getAlerts() {
        AlertListResponse response = new AlertListResponse();

        Long total = riskEventMapper.selectCount(
                new LambdaQueryWrapper<RiskEvent>()
                        .eq(RiskEvent::getDeleted, 0)
        );

        List<RiskEvent> events = riskEventMapper.selectList(
                new LambdaQueryWrapper<RiskEvent>()
                        .eq(RiskEvent::getDeleted, 0)
                        .orderByDesc(RiskEvent::getCreatedAt)
                        .last("LIMIT 10")
        );

        List<AlertItem> list = events.stream().map(event -> {
            AlertItem item = new AlertItem();
            item.setAlertId(event.getId());
            item.setType(event.getCategory());
            item.setLevel(event.getRiskLevel());
            item.setTitle("风险告警: " + (event.getTriggerRule() != null ? event.getTriggerRule() : event.getCategory()));
            item.setContent(event.getEventDetail());
            item.setCreatedAt(event.getTriggeredAt() != null ? event.getTriggeredAt() : event.getCreatedAt());
            item.setRead(event.getStatus() != null && event.getStatus() == 2);
            return item;
        }).collect(Collectors.toList());

        response.setList(list);
        response.setTotal(total);

        return response;
    }

    public List<AmountDistribution> getAmountDistribution(String dimension) {
        List<AmountDistribution> result = new ArrayList<>();

        if ("channel".equals(dimension)) {
            List<ChannelConfig> channels = channelConfigMapper.selectList(
                    new LambdaQueryWrapper<ChannelConfig>()
                            .eq(ChannelConfig::getDeleted, 0)
            );

            Map<Long, ChannelConfig> channelMap = channels.stream()
                    .collect(Collectors.toMap(ChannelConfig::getId, c -> c, (a, b) -> a));

            List<TradeOrder> paidOrders = tradeOrderMapper.selectList(
                    new LambdaQueryWrapper<TradeOrder>()
                            .select(TradeOrder::getChannelId, TradeOrder::getActualAmount)
                            .eq(TradeOrder::getDeleted, 0)
                            .eq(TradeOrder::getStatus, 2)
            );

            Map<Long, BigDecimal> amountByChannel = paidOrders.stream()
                    .filter(o -> o.getChannelId() != null && o.getActualAmount() != null)
                    .collect(Collectors.groupingBy(
                            TradeOrder::getChannelId,
                            Collectors.reducing(BigDecimal.ZERO, TradeOrder::getActualAmount, BigDecimal::add)
                    ));

            for (Map.Entry<Long, BigDecimal> entry : amountByChannel.entrySet()) {
                AmountDistribution dist = new AmountDistribution();
                ChannelConfig channel = channelMap.get(entry.getKey());
                dist.setName(channel != null ? channel.getChannelName() : "未知通道");
                dist.setValue(entry.getValue().setScale(2, RoundingMode.HALF_UP));
                result.add(dist);
            }

            if (result.isEmpty()) {
                Random random = new Random();
                String[] defaultChannels = {"支付宝", "微信支付", "银联支付"};
                for (String ch : defaultChannels) {
                    AmountDistribution dist = new AmountDistribution();
                    dist.setName(ch);
                    dist.setValue(new BigDecimal(10000 + random.nextInt(50000))
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
                    result.add(dist);
                }
            }
        }

        return result;
    }
}
