package com.paygateway.admin.config;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paygateway.admin.entity.*;
import com.paygateway.admin.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final MerchantInfoMapper merchantInfoMapper;
    private final ChannelConfigMapper channelConfigMapper;
    private final ChannelRouteMapper channelRouteMapper;
    private final TradeOrderMapper tradeOrderMapper;
    private final RefundOrderMapper refundOrderMapper;
    private final RiskRuleMapper riskRuleMapper;
    private final RiskEventMapper riskEventMapper;
    private final SystemConfigMapper systemConfigMapper;

    private static final DateTimeFormatter ORDER_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final Random random = new Random();

    @Override
    public void run(String... args) {
        initPermissions();
        initRoles();
        initAdminUser();
        initChannels();
        initChannelRoutes();
        initMerchants();
        initTradeOrders();
        initRefundOrders();
        initRiskRules();
        initRiskEvents();
        initSystemConfigs();
        log.info("========================================");
        log.info("  PayGateway 后端服务初始化完成！");
        log.info("  默认账号: admin / admin123");
        log.info("  API文档: http://localhost:8080/api/v1/doc.html");
        log.info("  H2控制台: http://localhost:8080/api/v1/h2-console");
        log.info("========================================");
    }

    private void initAdminUser() {
        Long count = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, "admin"));
        if (count > 0) {
            log.info("Admin user already exists, skip");
            return;
        }
        SysUser admin = new SysUser();
        admin.setUsername("admin");
        admin.setPassword(BCrypt.hashpw("admin123", BCrypt.gensalt()));
        admin.setRealName("系************");
        admin.setEmail("T1T2c@PjXkDekE.4铁");
        admin.setPhone("NOkT4YYwjyD");
        admin.setRoleCode("admin");
        admin.setStatus(1);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        admin.setDeleted(0);
        sysUserMapper.insert(admin);
        log.info("Admin user created: admin/admin123");
    }

    private void initChannels() {
        List<String> channelCodes = Arrays.asList("WECHAT", "ALIPAY", "UNIONPAY");
        for (String code : channelCodes) {
            Long count = channelConfigMapper.selectCount(new LambdaQueryWrapper<ChannelConfig>().eq(ChannelConfig::getChannelCode, code));
            if (count > 0) continue;

            ChannelConfig channel = new ChannelConfig();
            channel.setChannelCode(code);
            channel.setStatus(1);
            channel.setCreatedAt(LocalDateTime.now());
            channel.setUpdatedAt(LocalDateTime.now());
            channel.setDeleted(0);

            switch (code) {
                case "WECHAT":
                    channel.setChannelName("微信支付");
                    channel.setChannelType("1");
                    channel.setPayTypes("JSAPI,NATIVE,APP,H5");
                    channel.setPriority(100);
                    channel.setWeight(100);
                    channel.setAvgSuccessRate(new BigDecimal("99.85"));
                    channel.setAvgLatency(180L);
                    channel.setCurrentQps(0);
                    channel.setDailyAmount(new BigDecimal("8562300.50"));
                    channel.setDailyCount(12580L);
                    break;
                case "ALIPAY":
                    channel.setChannelName("支付宝");
                    channel.setChannelType("1");
                    channel.setPayTypes("JSAPI,NATIVE,APP,H5,WAP");
                    channel.setPriority(100);
                    channel.setWeight(100);
                    channel.setAvgSuccessRate(new BigDecimal("99.92"));
                    channel.setAvgLatency(150L);
                    channel.setCurrentQps(0);
                    channel.setDailyAmount(new BigDecimal("12356800.00"));
                    channel.setDailyCount(18650L);
                    break;
                case "UNIONPAY":
                    channel.setChannelName("银联云闪付");
                    channel.setChannelType("1");
                    channel.setPayTypes("APP,PC,H5");
                    channel.setPriority(90);
                    channel.setWeight(80);
                    channel.setAvgSuccessRate(new BigDecimal("99.50"));
                    channel.setAvgLatency(250L);
                    channel.setCurrentQps(0);
                    channel.setDailyAmount(new BigDecimal("3256000.00"));
                    channel.setDailyCount(4520L);
                    break;
            }
            channelConfigMapper.insert(channel);
        }
        log.info("Channels initialized: 3 payment channels");
    }

    private void initMerchants() {
        Long merchantCount = merchantInfoMapper.selectCount(null);
        if (merchantCount >= 5) {
            log.info("Merchants already exist, skip");
            return;
        }

        List<MerchantInfo> merchants = Arrays.asList(
                createMerchant("M100001", "北京****技有限公司", "星耀科技", "互联网", "91110108MA01ABC123", "张**", "张**", "138******01", "z****@***********", 1, 5, 1, "T+1", "LOW", new BigDecimal("5000000"), new BigDecimal("100000"), new BigDecimal("0.005500"), "优质互联网商户"),
                createMerchant("M100002", "上海云海电子商务有限公司", "云海电商", "电商零售", "91310115MA02DEF456", "李**", "李**", "138******02", "l*@************", 1, 4, 1, "T+1", "LOW", new BigDecimal("10000000"), new BigDecimal("200000"), new BigDecimal("0.006000"), "电商大客户"),
                createMerchant("M100003", "深圳智慧生活服务有限公司", "智慧生活", "生活服务", "91440300MA03GHI789", "王**", "王**", "138******03", "w****@************", 1, 3, 1, "T+1", "MEDIUM", new BigDecimal("2000000"), new BigDecimal("50000"), new BigDecimal("0.006500"), "生活服务类商户"),
                createMerchant("M100004", "杭州未来教育科技有限公司", "未来教育", "教育培训", "91330106MA04JKL012", "陈**", "陈**", "138******04", "c****@************", 1, 4, 1, "T+1", "LOW", new BigDecimal("3000000"), new BigDecimal("80000"), new BigDecimal("0.005000"), "教育类优质商户"),
                createMerchant("M100005", "广州优选贸易有限公司", "优选贸易", "批发零售", "91440101MA05MNO345", "赵**", "赵**", "138******05", "z****@************", 0, 2, 0, "T+1", "MEDIUM", new BigDecimal("500000"), new BigDecimal("10000"), new BigDecimal("0.006000"), "待审核商户"),
                createMerchant("M100006", "成都美食汇餐饮管理公司", "美食汇", "餐饮美食", "91510104MA06PQR678", "刘**", "刘**", "138******06", "l**@************", 1, 3, 1, "T+1", "MEDIUM", new BigDecimal("200000"), new BigDecimal("5000"), new BigDecimal("0.007000"), "餐饮连锁商户"),
                createMerchant("M100007", "南京文旅发展集团", "南京文旅", "文旅", "91320100MA07STU901", "吴**", "吴**", "138******07", "w*@************", 1, 5, 1, "T+7", "LOW", new BigDecimal("8000000"), new BigDecimal("200000"), new BigDecimal("0.004500"), "文旅集团大客户"),
                createMerchant("M100008", "武汉康瑞医疗器械公司", "康瑞医疗", "医疗健康", "91420106MA08VWX234", "周**", "周**", "138******08", "z****@************", 4, 2, 0, "T+1", "HIGH", new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("0.006000"), "资质待审核驳回")
        );

        for (MerchantInfo m : merchants) {
            merchantInfoMapper.insert(m);
        }
        log.info("Merchants initialized: {} records", merchants.size());
    }

    private MerchantInfo createMerchant(String merchantNo, String merchantName, String shortName,
                                        String industry, String license, String legalPerson,
                                        String contactName, String contactPhone, String contactEmail,
                                        Integer status, Integer level, Integer authStatus,
                                        String settleCycle, String riskLevel, BigDecimal dailyLimit,
                                        BigDecimal singleLimit, BigDecimal feeRate, String remark) {
        MerchantInfo m = new MerchantInfo();
        m.setMerchantNo(merchantNo);
        m.setMerchantName(merchantName);
        m.setShortName(shortName);
        m.setIndustry(industry);
        m.setBusinessLicense(license);
        m.setLegalPerson(legalPerson);
        m.setContactName(contactName);
        m.setContactPhone(contactPhone);
        m.setContactEmail(contactEmail);
        m.setStatus(status);
        m.setLevel(level);
        m.setAuthStatus(authStatus);
        m.setSettleCycle(settleCycle);
        m.setRiskLevel(riskLevel);
        m.setDailyLimit(dailyLimit);
        m.setSingleLimit(singleLimit);
        m.setFeeRate(feeRate);
        m.setRemark(remark);
        if (status == 1) {
            m.setAuditRemark("审核通过，资质齐全");
            m.setAuditTime(LocalDateTime.now().minusDays(random.nextInt(30)));
        } else if (status == 4) {
            m.setAuditRemark("营业执照信息不完整，请补充后重新提交");
            m.setAuditTime(LocalDateTime.now().minusDays(2));
        }
        m.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(30) + 1));
        m.setUpdatedAt(LocalDateTime.now());
        m.setDeleted(0);
        return m;
    }

    private void initTradeOrders() {
        Long orderCount = tradeOrderMapper.selectCount(null);
        if (orderCount >= 10) {
            log.info("Trade orders already exist, skip");
            return;
        }

        String[] merchantNos = {"M100001", "M100002", "M100003", "M100004", "M100007"};
        Long[] merchantIds = {1L, 2L, 3L, 4L, 7L};
        String[] channels = {"WECHAT", "ALIPAY", "UNIONPAY"};
        Long[] channelIds = {1L, 2L, 3L};
        String[] payTypes = {"JSAPI", "NATIVE", "APP", "H5"};
        String[] subjects = {"会员充值", "商品购买", "服务费用支付", "课程购买", "餐饮消费", "酒店预订", "门票购买", "订单结算"};
        Integer[] statuses = {2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 3, 4, 5, 2, 2};
        BigDecimal[] amounts = {
                new BigDecimal("99.00"), new BigDecimal("299.00"), new BigDecimal("1580.00"),
                new BigDecimal("1280.00"), new BigDecimal("58.00"), new BigDecimal("999.00"),
                new BigDecimal("199.00"), new BigDecimal("2580.00"), new BigDecimal("68.00"),
                new BigDecimal("499.00"), new BigDecimal("88.00"), new BigDecimal("1.00"),
                new BigDecimal("368.00"), new BigDecimal("12800.00"), new BigDecimal("256.80")
        };

        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < amounts.length; i++) {
            TradeOrder order = new TradeOrder();
            String orderNo = "PAY" + now.minusHours(i * 2 + 1).format(ORDER_NO_FMT) + String.format("%04d", i + 1);
            order.setOrderNo(orderNo);
            order.setMerchantOrderNo("MO" + System.currentTimeMillis() + i);
            int merchIdx = random.nextInt(merchantNos.length);
            order.setMerchantId(merchantIds[merchIdx]);
            order.setMerchantNo(merchantNos[merchIdx]);
            int chanIdx = random.nextInt(channels.length);
            order.setChannelId(channelIds[chanIdx]);
            order.setChannelCode(channels[chanIdx]);
            order.setPayType(payTypes[random.nextInt(payTypes.length)]);
            order.setSubject(subjects[random.nextInt(subjects.length)]);
            order.setBody(order.getSubject() + " - 订单详情");
            order.setAmount(amounts[i]);
            order.setCurrency("CNY");
            order.setClientIp("192.168." + (random.nextInt(255) + 1) + "." + (random.nextInt(254) + 1));
            order.setNotifyUrl("https://api.example.com/notify");
            order.setReturnUrl("https://www.example.com/return");
            order.setExpireTime(now.minusHours(i * 2 + 1).plusMinutes(30));
            order.setStatus(statuses[i]);
            order.setDeleted(0);

            if (statuses[i] == 2 || statuses[i] == 5 || statuses[i] == 6) {
                order.setActualAmount(amounts[i]);
                BigDecimal rate = merchantIds[merchIdx].equals(7L) ? new BigDecimal("0.0045") : new BigDecimal("0.006");
                order.setFeeRate(rate);
                order.setFee(amounts[i].multiply(rate).setScale(2, RoundingMode.HALF_UP));
                order.setPaidAt(now.minusHours(i * 2 + 1).plusMinutes(random.nextInt(10)));
                order.setChannelOrderNo("CH" + orderNo.substring(3));
            } else if (statuses[i] == 3) {
                order.setChannelResponse("{\"code\":\"FAIL\",\"msg\":\"支付失败：余额不足\"}");
            }

            order.setCreatedAt(now.minusHours(i * 2 + 1));
            order.setUpdatedAt(now.minusHours(i * 2 + 1).plusMinutes(random.nextInt(15)));
            tradeOrderMapper.insert(order);
        }
        log.info("Trade orders initialized: {} records", amounts.length);
    }

    private void initRefundOrders() {
        Long refundCount = refundOrderMapper.selectCount(null);
        if (refundCount > 0) {
            log.info("Refund orders already exist, skip");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<RefundOrder> refunds = Arrays.asList(
                createRefundOrder("REF" + now.minusHours(5).format(ORDER_NO_FMT) + "001",
                        "PAY" + now.minusHours(6).format(ORDER_NO_FMT) + "007", 7L, "M100007",
                        "ALIPAY", new BigDecimal("199.00"), "客户申请退款（未消费）",
                        1, now.minusHours(3)),
                createRefundOrder("REF" + now.minusHours(12).format(ORDER_NO_FMT) + "002",
                        "PAY" + now.minusHours(14).format(ORDER_NO_FMT) + "013", 2L, "M100002",
                        "WECHAT", new BigDecimal("88.00"), "商品质量问题退款",
                        0, null)
        );
        for (RefundOrder r : refunds) {
            refundOrderMapper.insert(r);
        }
        log.info("Refund orders initialized: {} records", refunds.size());
    }

    private RefundOrder createRefundOrder(String refundNo, String orderNo, Long merchantId, String merchantNo,
                                          String channelCode, BigDecimal amount, String reason,
                                          Integer status, LocalDateTime refundedAt) {
        RefundOrder r = new RefundOrder();
        r.setRefundNo(refundNo);
        r.setOrderNo(orderNo);
        r.setMerchantId(merchantId);
        r.setMerchantNo(merchantNo);
        r.setChannelCode(channelCode);
        r.setRefundAmount(amount);
        r.setRefundFee(amount.multiply(new BigDecimal("0.006")).setScale(2, RoundingMode.HALF_UP));
        r.setRefundReason(reason);
        r.setStatus(status);
        r.setRefundedAt(refundedAt);
        r.setCreatedAt(refundedAt != null ? refundedAt.minusHours(1) : LocalDateTime.now().minusHours(6));
        r.setUpdatedAt(LocalDateTime.now());
        r.setDeleted(0);
        return r;
    }

    private void initRiskRules() {
        Long ruleCount = riskRuleMapper.selectCount(null);
        if (ruleCount > 0) {
            log.info("Risk rules already exist, skip");
            return;
        }

        List<RiskRule> rules = Arrays.asList(
                createRiskRule("RULE_AMOUNT_LIMIT", "大额交易监控", "TRADE_LIMIT",
                        "{\"conditions\":[{\"field\":\"amount\",\"operator\":\"GT\",\"value\":50000}],\"logic\":\"AND\"}",
                        "REVIEW", "HIGH", 1, "单笔金额超过5万元需人工审核"),
                createRiskRule("RULE_FREQ_LIMIT", "高频交易监控", "FREQUENCY",
                        "{\"conditions\":[{\"field\":\"minute_count\",\"operator\":\"GT\",\"value\":20}],\"logic\":\"AND\"}",
                        "ALERT", "MEDIUM", 2, "5分钟内交易超过20笔触发告警"),
                createRiskRule("RULE_IP_BLACKLIST", "IP黑名单拦截", "IP",
                        "{\"conditions\":[{\"field\":\"client_ip\",\"operator\":\"IN\",\"value\":\"blacklist_ips\"}],\"logic\":\"AND\"}",
                        "BLOCK", "HIGH", 3, "黑名单IP禁止交易"),
                createRiskRule("RULE_MIDNIGHT", "深夜大额交易监控", "GEO",
                        "{\"conditions\":[{\"field\":\"amount\",\"operator\":\"GT\",\"value\":10000},{\"field\":\"hour\",\"operator\":\"BETWEEN\",\"value\":\"0,5\"}],\"logic\":\"AND\"}",
                        "ALERT", "MEDIUM", 4, "凌晨0-5点大额交易告警")
        );
        for (RiskRule rule : rules) {
            riskRuleMapper.insert(rule);
        }
        log.info("Risk rules initialized: {} records", rules.size());
    }

    private RiskRule createRiskRule(String code, String name, String category, String expr,
                                    String action, String level, Integer priority, String desc) {
        RiskRule r = new RiskRule();
        r.setRuleCode(code);
        r.setRuleName(name);
        r.setCategory(category);
        r.setConditionExpr(expr);
        r.setAction(action);
        r.setRiskLevel(level);
        r.setPriority(priority);
        r.setStatus(1);
        r.setDescription(desc);
        r.setCreatedAt(LocalDateTime.now());
        r.setUpdatedAt(LocalDateTime.now());
        r.setDeleted(0);
        return r;
    }

    private void initRiskEvents() {
        Long eventCount = riskEventMapper.selectCount(null);
        if (eventCount > 0) {
            log.info("Risk events already exist, skip");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<RiskEvent> events = Arrays.asList(
                createRiskEvent("EVT" + now.minusHours(2).format(ORDER_NO_FMT) + "001",
                        "TRADE_LIMIT", "HIGH", null, "M100007", "RULE_AMOUNT_LIMIT",
                        "商户M100007单笔交易金额12800元，触发大额监控规则", 1, null, now.minusHours(2)),
                createRiskEvent("EVT" + now.minusHours(5).format(ORDER_NO_FMT) + "002",
                        "FREQUENCY", "MEDIUM", null, "M100001", "RULE_FREQ_LIMIT",
                        "商户M100001短时间内交易频繁，触发频率告警", 0, null, now.minusHours(5)),
                createRiskEvent("EVT" + now.minusDays(1).format(ORDER_NO_FMT) + "003",
                        "IP", "HIGH", null, null, "RULE_IP_BLACKLIST",
                        "黑名单IP 192.168.99.99 尝试发起交易已拦截", 2, "已确认为恶意IP，加入永久黑名单", now.minusDays(1))
        );
        for (RiskEvent e : events) {
            riskEventMapper.insert(e);
        }
        log.info("Risk events initialized: {} records", events.size());
    }

    private RiskEvent createRiskEvent(String eventNo, String category, String level, String orderNo,
                                      String merchantNo, String triggerRule, String detail,
                                      Integer status, String handleNote, LocalDateTime triggeredAt) {
        RiskEvent e = new RiskEvent();
        e.setEventNo(eventNo);
        e.setCategory(category);
        e.setRiskLevel(level);
        e.setOrderNo(orderNo);
        e.setMerchantNo(merchantNo);
        e.setTriggerRule(triggerRule);
        e.setEventDetail(detail);
        e.setStatus(status);
        e.setHandleNote(handleNote);
        e.setTriggeredAt(triggeredAt);
        if (status == 2) {
            e.setHandledAt(triggeredAt.plusMinutes(30));
        }
        e.setCreatedAt(triggeredAt);
        e.setUpdatedAt(LocalDateTime.now());
        e.setDeleted(0);
        return e;
    }

    private void initChannelRoutes() {
        Long routeCount = channelRouteMapper.selectCount(null);
        if (routeCount > 0) {
            log.info("Channel routes already exist, skip");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<ChannelRoute> routes = Arrays.asList(
                createChannelRoute("RT" + now.format(ORDER_NO_FMT) + "001", "微信-大额优先", 1L, "WECHAT", null,
                        new BigDecimal("1000"), new BigDecimal("500000"), 100, 1, null, null, "单笔1000元以上优先走微信主通道"),
                createChannelRoute("RT" + now.format(ORDER_NO_FMT) + "002", "支付宝-默认路由", 2L, "ALIPAY", null,
                        new BigDecimal("0.01"), new BigDecimal("500000"), 100, 1, null, null, "支付宝默认通道路由"),
                createChannelRoute("RT" + now.format(ORDER_NO_FMT) + "003", "银联-大额备用", 3L, "UNIONPAY", null,
                        new BigDecimal("50000"), new BigDecimal("500000"), 80, 1, null, null, "5万以上大额备用走银联"),
                createChannelRoute("RT" + now.format(ORDER_NO_FMT) + "004", "微信-JSAPI专用", 1L, "WECHAT", "JSAPI",
                        new BigDecimal("0.01"), new BigDecimal("1000"), 100, 1, null, null, "微信公众号JSAPI支付专用路由"),
                createChannelRoute("RT" + now.format(ORDER_NO_FMT) + "005", "支付宝-H5专用", 2L, "ALIPAY", "H5",
                        new BigDecimal("0.01"), new BigDecimal("50000"), 100, 1, null, null, "支付宝H5移动端支付专用路由")
        );
        for (ChannelRoute r : routes) {
            channelRouteMapper.insert(r);
        }
        log.info("Channel routes initialized: {} records", routes.size());
    }

    private ChannelRoute createChannelRoute(String routeNo, String routeName, Long channelId, String channelCode,
                                             String payType, BigDecimal minAmount, BigDecimal maxAmount,
                                             Integer priority, Integer status, String timeStart, String timeEnd, String remark) {
        ChannelRoute r = new ChannelRoute();
        r.setRouteNo(routeNo);
        r.setRouteName(routeName);
        r.setChannelId(channelId);
        r.setChannelCode(channelCode);
        r.setPayType(payType);
        r.setMinAmount(minAmount);
        r.setMaxAmount(maxAmount);
        r.setPriority(priority);
        r.setStatus(status);
        r.setTimeStart(timeStart);
        r.setTimeEnd(timeEnd);
        r.setRemark(remark);
        r.setCreatedAt(LocalDateTime.now());
        r.setUpdatedAt(LocalDateTime.now());
        r.setDeleted(0);
        return r;
    }

    private void initSystemConfigs() {
        Long cfgCount = systemConfigMapper.selectCount(null);
        if (cfgCount > 0) {
            log.info("System configs already exist, skip");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<SystemConfig> configs = Arrays.asList(
                createSystemConfig("site.name", "PayGateway支付网关", "BASIC", "系统名称"),
                createSystemConfig("site.logo", "/logo.png", "BASIC", "系统Logo"),
                createSystemConfig("order.expire.minutes", "30", "TRADE", "订单超时时间（分钟）"),
                createSystemConfig("refund.auto.approve", "false", "TRADE", "退款自动审批"),
                createSystemConfig("notify.url", "https://api.paygateway.com/notify", "NOTIFY", "回调通知地址"),
                createSystemConfig("notify.retry.times", "5", "NOTIFY", "通知重试次数"),
                createSystemConfig("risk.enabled", "true", "RISK", "风控开关"),
                createSystemConfig("risk.score.high", "80", "RISK", "高风险分数阈值"),
                createSystemConfig("settle.auto", "true", "SETTLE", "自动结算开关"),
                createSystemConfig("settle.time", "02:00", "SETTLE", "每日结算时间")
        );
        for (SystemConfig c : configs) {
            c.setCreatedAt(now);
            c.setUpdatedAt(now);
            systemConfigMapper.insert(c);
        }
        log.info("System configs initialized: {} records", configs.size());
    }

    private SystemConfig createSystemConfig(String key, String value, String group, String desc) {
        SystemConfig c = new SystemConfig();
        c.setConfigKey(key);
        c.setConfigValue(value);
        c.setConfigGroup(group);
        c.setDescription(desc);
        return c;
    }

    private void initPermissions() {
        Long count = sysPermissionMapper.selectCount(null);
        if (count > 0) {
            log.info("Permissions already exist, skip");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        Map<String, Long> permIdMap = new HashMap<>();

        // ========== 一级菜单 ==========
        permIdMap.put("dashboard", insertPermission(0L, "dashboard", "仪表盘", 1, "/dashboard", "dashboard/index", "Odometer", 1, now));
        permIdMap.put("merchant", insertPermission(0L, "merchant", "商户管理", 1, "/merchant", null, "Shop", 2, now));
        permIdMap.put("channel", insertPermission(0L, "channel", "通道管理", 1, "/channel", null, "Connection", 3, now));
        permIdMap.put("trade", insertPermission(0L, "trade", "交易管理", 1, "/transaction", null, "Money", 4, now));
        permIdMap.put("risk", insertPermission(0L, "risk", "风控管理", 1, "/risk", null, "Warning", 5, now));
        permIdMap.put("recon", insertPermission(0L, "recon", "对账管理", 1, "/reconciliation", null, "Files", 6, now));
        permIdMap.put("system", insertPermission(0L, "system", "系统管理", 1, "/system", null, "Setting", 7, now));

        // ========== 仪表盘权限点 ==========
        insertPermission(permIdMap.get("dashboard"), "dashboard:view", "查看仪表盘", 3, null, null, null, 1, now);

        // ========== 商户管理 ==========
        Long merchantMenu = permIdMap.get("merchant");
        Long merchantListMenu = insertPermission(merchantMenu, "merchant:list", "商户列表", 2, "/merchant/list", "merchant/list", "List", 1, now);
        Long merchantAuditMenu = insertPermission(merchantMenu, "merchant:audit", "商户审核", 2, "/merchant/audit", "merchant/audit", "CircleCheck", 2, now);
        insertPermission(merchantListMenu, "merchant:list:view", "查看商户列表", 3, null, null, null, 1, now);
        insertPermission(merchantListMenu, "merchant:add", "新增商户", 3, null, null, null, 2, now);
        insertPermission(merchantListMenu, "merchant:edit", "编辑商户", 3, null, null, null, 3, now);
        insertPermission(merchantListMenu, "merchant:remove", "删除商户", 3, null, null, null, 4, now);
        insertPermission(merchantAuditMenu, "merchant:audit:view", "查看审核列表", 3, null, null, null, 1, now);
        insertPermission(merchantAuditMenu, "merchant:audit:approve", "审核通过", 3, null, null, null, 2, now);
        insertPermission(merchantAuditMenu, "merchant:audit:reject", "审核拒绝", 3, null, null, null, 3, now);

        // ========== 通道管理 ==========
        Long channelMenu = permIdMap.get("channel");
        Long channelListMenu = insertPermission(channelMenu, "channel:list", "通道配置", 2, "/channel/list", "channel/list", "Setting", 1, now);
        Long channelRouteMenu = insertPermission(channelMenu, "channel:route", "通道路由", 2, "/channel/route", "channel/route", "Guide", 2, now);
        insertPermission(channelListMenu, "channel:list:view", "查看通道列表", 3, null, null, null, 1, now);
        insertPermission(channelListMenu, "channel:add", "新增通道", 3, null, null, null, 2, now);
        insertPermission(channelListMenu, "channel:edit", "编辑通道", 3, null, null, null, 3, now);
        insertPermission(channelListMenu, "channel:remove", "删除通道", 3, null, null, null, 4, now);
        insertPermission(channelRouteMenu, "channel:route:view", "查看路由", 3, null, null, null, 1, now);
        insertPermission(channelRouteMenu, "channel:route:add", "新增路由", 3, null, null, null, 2, now);
        insertPermission(channelRouteMenu, "channel:route:edit", "编辑路由", 3, null, null, null, 3, now);
        insertPermission(channelRouteMenu, "channel:route:remove", "删除路由", 3, null, null, null, 4, now);

        // ========== 交易管理 ==========
        Long tradeMenu = permIdMap.get("trade");
        Long tradeOrderMenu = insertPermission(tradeMenu, "trade:order", "交易订单", 2, "/transaction/list", "transaction/list", "Document", 1, now);
        Long tradeRefundMenu = insertPermission(tradeMenu, "trade:refund", "退款订单", 2, "/transaction/refund", "transaction/refund", "RefreshLeft", 2, now);
        insertPermission(tradeOrderMenu, "trade:order:view", "查看订单", 3, null, null, null, 1, now);
        insertPermission(tradeOrderMenu, "trade:order:query", "查询订单", 3, null, null, null, 2, now);
        insertPermission(tradeOrderMenu, "trade:order:export", "导出订单", 3, null, null, null, 3, now);
        insertPermission(tradeRefundMenu, "trade:refund:view", "查看退款", 3, null, null, null, 1, now);
        insertPermission(tradeRefundMenu, "trade:refund:add", "申请退款", 3, null, null, null, 2, now);
        insertPermission(tradeRefundMenu, "trade:refund:audit", "审核退款", 3, null, null, null, 3, now);

        // ========== 风控管理 ==========
        Long riskMenu = permIdMap.get("risk");
        Long riskRulesMenu = insertPermission(riskMenu, "risk:rules", "风控规则", 2, "/risk/rules", "risk/rules", "DocumentCopy", 1, now);
        Long riskEventsMenu = insertPermission(riskMenu, "risk:events", "风控事件", 2, "/risk/events", "risk/events", "Bell", 2, now);
        insertPermission(riskRulesMenu, "risk:rules:view", "查看规则", 3, null, null, null, 1, now);
        insertPermission(riskRulesMenu, "risk:rules:add", "新增规则", 3, null, null, null, 2, now);
        insertPermission(riskRulesMenu, "risk:rules:edit", "编辑规则", 3, null, null, null, 3, now);
        insertPermission(riskRulesMenu, "risk:rules:remove", "删除规则", 3, null, null, null, 4, now);
        insertPermission(riskEventsMenu, "risk:events:view", "查看事件", 3, null, null, null, 1, now);
        insertPermission(riskEventsMenu, "risk:events:handle", "处理事件", 3, null, null, null, 2, now);

        // ========== 对账管理 ==========
        Long reconMenu = permIdMap.get("recon");
        Long reconTasksMenu = insertPermission(reconMenu, "recon:tasks", "对账任务", 2, "/reconciliation/center", "reconciliation/center", "Operation", 1, now);
        Long reconReportsMenu = insertPermission(reconMenu, "recon:reports", "对账报表", 2, "/reconciliation/report", "reconciliation/report", "DataAnalysis", 2, now);
        insertPermission(reconTasksMenu, "recon:tasks:view", "查看任务", 3, null, null, null, 1, now);
        insertPermission(reconTasksMenu, "recon:tasks:create", "创建任务", 3, null, null, null, 2, now);
        insertPermission(reconTasksMenu, "recon:tasks:handle", "处理差异", 3, null, null, null, 3, now);
        insertPermission(reconReportsMenu, "recon:reports:view", "查看报表", 3, null, null, null, 1, now);

        // ========== 系统管理 ==========
        Long systemMenu = permIdMap.get("system");
        Long sysUserMenu = insertPermission(systemMenu, "system:user", "用户管理", 2, "/system/users", "system/users", "User", 1, now);
        Long sysRoleMenu = insertPermission(systemMenu, "system:role", "角色管理", 2, "/system/roles", "system/roles", "UserFilled", 2, now);
        Long sysPermMenu = insertPermission(systemMenu, "system:permission", "权限管理", 2, "/system/permissions", "system/permissions", "Key", 3, now);
        Long sysConfigMenu = insertPermission(systemMenu, "system:config", "系统配置", 2, "/system/settings", "system/settings", "Tools", 4, now);
        // 用户管理权限点
        insertPermission(sysUserMenu, "system:user:list", "用户列表", 3, null, null, null, 1, now);
        insertPermission(sysUserMenu, "system:user:query", "用户查询", 3, null, null, null, 2, now);
        insertPermission(sysUserMenu, "system:user:add", "新增用户", 3, null, null, null, 3, now);
        insertPermission(sysUserMenu, "system:user:edit", "编辑用户", 3, null, null, null, 4, now);
        insertPermission(sysUserMenu, "system:user:remove", "删除用户", 3, null, null, null, 5, now);
        insertPermission(sysUserMenu, "system:user:resetPwd", "重置密码", 3, null, null, null, 6, now);
        // 角色管理权限点
        insertPermission(sysRoleMenu, "system:role:list", "角色列表", 3, null, null, null, 1, now);
        insertPermission(sysRoleMenu, "system:role:query", "角色查询", 3, null, null, null, 2, now);
        insertPermission(sysRoleMenu, "system:role:add", "新增角色", 3, null, null, null, 3, now);
        insertPermission(sysRoleMenu, "system:role:edit", "编辑角色", 3, null, null, null, 4, now);
        insertPermission(sysRoleMenu, "system:role:remove", "删除角色", 3, null, null, null, 5, now);
        // 权限管理权限点
        insertPermission(sysPermMenu, "system:permission:list", "权限列表", 3, null, null, null, 1, now);
        insertPermission(sysPermMenu, "system:permission:query", "权限查询", 3, null, null, null, 2, now);
        insertPermission(sysPermMenu, "system:permission:add", "新增权限", 3, null, null, null, 3, now);
        insertPermission(sysPermMenu, "system:permission:edit", "编辑权限", 3, null, null, null, 4, now);
        insertPermission(sysPermMenu, "system:permission:remove", "删除权限", 3, null, null, null, 5, now);
        // 系统配置权限点
        insertPermission(sysConfigMenu, "system:config:view", "查看配置", 3, null, null, null, 1, now);
        insertPermission(sysConfigMenu, "system:config:edit", "修改配置", 3, null, null, null, 2, now);

        log.info("Permissions initialized");
    }

    private Long insertPermission(Long parentId, String code, String name, Integer type,
                                   String path, String component, String icon, Integer sort, LocalDateTime now) {
        SysPermission p = new SysPermission();
        p.setParentId(parentId);
        p.setPermissionCode(code);
        p.setPermissionName(name);
        p.setPermissionType(type);
        p.setPath(path);
        p.setComponent(component);
        p.setIcon(icon);
        p.setSortOrder(sort);
        p.setStatus(1);
        p.setVisible(1);
        p.setDeleted(0);
        p.setCreatedAt(now);
        p.setUpdatedAt(now);
        sysPermissionMapper.insert(p);
        return p.getId();
    }

    private void initRoles() {
        Long roleCount = sysRoleMapper.selectCount(null);
        if (roleCount > 0) {
            log.info("Roles already exist, skip");
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        // 创建超级管理员角色
        SysRole adminRole = new SysRole();
        adminRole.setRoleCode("admin");
        adminRole.setRoleName("超级管理员");
        adminRole.setDescription("拥有所有权限");
        adminRole.setDataScope(1);
        adminRole.setStatus(1);
        adminRole.setSortOrder(1);
        adminRole.setCreatedAt(now);
        adminRole.setUpdatedAt(now);
        adminRole.setDeleted(0);
        sysRoleMapper.insert(adminRole);

        // 创建运营角色
        SysRole operatorRole = new SysRole();
        operatorRole.setRoleCode("operator");
        operatorRole.setRoleName("运营人员");
        operatorRole.setDescription("负责日常运营操作");
        operatorRole.setDataScope(2);
        operatorRole.setStatus(1);
        operatorRole.setSortOrder(2);
        operatorRole.setCreatedAt(now);
        operatorRole.setUpdatedAt(now);
        operatorRole.setDeleted(0);
        sysRoleMapper.insert(operatorRole);

        // 创建风控角色
        SysRole riskRole = new SysRole();
        riskRole.setRoleCode("risk");
        riskRole.setRoleName("风控专员");
        riskRole.setDescription("负责风控管理");
        riskRole.setDataScope(2);
        riskRole.setStatus(1);
        riskRole.setSortOrder(3);
        riskRole.setCreatedAt(now);
        riskRole.setUpdatedAt(now);
        riskRole.setDeleted(0);
        sysRoleMapper.insert(riskRole);

        // 给运营角色分配基础权限
        List<String> operatorPerms = Arrays.asList(
                "dashboard:view",
                "merchant:list:view", "merchant:audit:view",
                "channel:list:view", "channel:route:view",
                "trade:order:view", "trade:order:query",
                "trade:refund:view", "trade:refund:add",
                "risk:rules:view", "risk:events:view",
                "recon:tasks:view", "recon:reports:view"
        );
        assignRolePermissions(operatorRole.getId(), operatorPerms, now);

        // 给风控角色分配风控相关权限
        List<String> riskPerms = Arrays.asList(
                "dashboard:view",
                "risk:rules:view", "risk:rules:add", "risk:rules:edit",
                "risk:events:view", "risk:events:handle",
                "trade:order:view", "trade:refund:view", "trade:refund:audit"
        );
        assignRolePermissions(riskRole.getId(), riskPerms, now);

        // 给admin用户分配admin角色
        SysUser adminUser = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, "admin")
        );
        if (adminUser != null) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(adminUser.getId());
            ur.setRoleId(adminRole.getId());
            ur.setCreatedAt(now);
            sysUserRoleMapper.insert(ur);
        }

        log.info("Roles initialized: 3 roles (admin, operator, risk)");
    }

    private void assignRolePermissions(Long roleId, List<String> permCodes, LocalDateTime now) {
        if (permCodes == null || permCodes.isEmpty()) return;
        List<SysPermission> perms = sysPermissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>()
                        .in(SysPermission::getPermissionCode, permCodes)
                        .eq(SysPermission::getDeleted, 0)
        );
        for (SysPermission p : perms) {
            SysRolePermission rp = new SysRolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(p.getId());
            rp.setCreatedAt(now);
            sysRolePermissionMapper.insert(rp);
        }
    }
}
