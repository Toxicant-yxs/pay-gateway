package com.paygateway.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paygateway.admin.entity.MerchantInfo;
import com.paygateway.admin.entity.RiskBlacklist;
import com.paygateway.admin.entity.RiskEvent;
import com.paygateway.admin.entity.RiskRule;
import com.paygateway.admin.mapper.MerchantInfoMapper;
import com.paygateway.admin.mapper.RiskBlacklistMapper;
import com.paygateway.admin.mapper.RiskEventMapper;
import com.paygateway.admin.mapper.RiskRuleMapper;
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
public class RiskService {

    private final RiskRuleMapper riskRuleMapper;
    private final RiskEventMapper riskEventMapper;
    private final RiskBlacklistMapper riskBlacklistMapper;
    private final MerchantInfoMapper merchantInfoMapper;

    public PageResult<RiskRule> listRules(PageQuery pageQuery, String category, Integer status) {
        Page<RiskRule> page = new Page<>(pageQuery.getPage(), pageQuery.getPageSize());
        LambdaQueryWrapper<RiskRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RiskRule::getDeleted, 0);
        if (StringUtils.hasText(category)) {
            wrapper.eq(RiskRule::getCategory, category);
        }
        if (status != null) {
            wrapper.eq(RiskRule::getStatus, status);
        }
        wrapper.orderByAsc(RiskRule::getPriority);
        Page<RiskRule> result = riskRuleMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), pageQuery.getPage(), pageQuery.getPageSize());
    }

    public RiskRule createRule(RiskRule rule) {
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(LocalDateTime.now());
        rule.setDeleted(0);
        if (rule.getStatus() == null) rule.setStatus(1);
        riskRuleMapper.insert(rule);
        return rule;
    }

    public RiskRule updateRule(Long id, RiskRule rule) {
        RiskRule existing = getRuleById(id);
        if (StringUtils.hasText(rule.getRuleName())) existing.setRuleName(rule.getRuleName());
        if (StringUtils.hasText(rule.getCategory())) existing.setCategory(rule.getCategory());
        if (rule.getConditionExpr() != null) existing.setConditionExpr(rule.getConditionExpr());
        if (StringUtils.hasText(rule.getAction())) existing.setAction(rule.getAction());
        if (StringUtils.hasText(rule.getRiskLevel())) existing.setRiskLevel(rule.getRiskLevel());
        if (rule.getPriority() != null) existing.setPriority(rule.getPriority());
        if (rule.getStatus() != null) existing.setStatus(rule.getStatus());
        if (rule.getDescription() != null) existing.setDescription(rule.getDescription());
        existing.setUpdatedAt(LocalDateTime.now());
        riskRuleMapper.updateById(existing);
        return existing;
    }

    public void updateRuleStatus(Long id, Integer status) {
        RiskRule rule = getRuleById(id);
        rule.setStatus(status);
        rule.setUpdatedAt(LocalDateTime.now());
        riskRuleMapper.updateById(rule);
    }

    public RiskRule getRuleById(Long id) {
        RiskRule rule = riskRuleMapper.selectOne(
                new LambdaQueryWrapper<RiskRule>()
                        .eq(RiskRule::getId, id)
                        .eq(RiskRule::getDeleted, 0)
        );
        if (rule == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return rule;
    }

    public PageResult<RiskEvent> listEvents(PageQuery pageQuery, String category, String riskLevel,
                                             String merchantNo, Integer status, LocalDateTime startTime, LocalDateTime endTime) {
        Page<RiskEvent> page = new Page<>(pageQuery.getPage(), pageQuery.getPageSize());
        LambdaQueryWrapper<RiskEvent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RiskEvent::getDeleted, 0);
        if (StringUtils.hasText(category)) {
            wrapper.eq(RiskEvent::getCategory, category);
        }
        if (StringUtils.hasText(riskLevel)) {
            wrapper.eq(RiskEvent::getRiskLevel, riskLevel);
        }
        if (StringUtils.hasText(merchantNo)) {
            wrapper.eq(RiskEvent::getMerchantNo, merchantNo);
        }
        if (status != null) {
            wrapper.eq(RiskEvent::getStatus, status);
        }
        if (startTime != null) {
            wrapper.ge(RiskEvent::getTriggeredAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(RiskEvent::getTriggeredAt, endTime);
        }
        wrapper.orderByDesc(RiskEvent::getTriggeredAt);
        Page<RiskEvent> result = riskEventMapper.selectPage(page, wrapper);
        enrichEvents(result.getRecords());
        return PageResult.of(result.getRecords(), result.getTotal(), pageQuery.getPage(), pageQuery.getPageSize());
    }

    public RiskEvent getEventById(Long id) {
        RiskEvent event = riskEventMapper.selectOne(
                new LambdaQueryWrapper<RiskEvent>()
                        .eq(RiskEvent::getId, id)
                        .eq(RiskEvent::getDeleted, 0)
        );
        if (event == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        enrichEvents(Collections.singletonList(event));
        return event;
    }

    private void enrichEvents(List<RiskEvent> events) {
        if (events == null || events.isEmpty()) return;

        Set<String> merchantNos = events.stream()
                .map(RiskEvent::getMerchantNo)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        Map<String, String> merchantNameMap = new HashMap<>();
        if (!merchantNos.isEmpty()) {
            List<MerchantInfo> merchants = merchantInfoMapper.selectList(
                    new LambdaQueryWrapper<MerchantInfo>()
                            .select(MerchantInfo::getMerchantNo, MerchantInfo::getMerchantName)
                            .in(MerchantInfo::getMerchantNo, merchantNos)
            );
            merchantNameMap = merchants.stream()
                    .collect(Collectors.toMap(MerchantInfo::getMerchantNo, MerchantInfo::getMerchantName, (a, b) -> a));
        }

        for (RiskEvent event : events) {
            if (StringUtils.hasText(event.getMerchantNo())) {
                event.setMerchantName(merchantNameMap.getOrDefault(event.getMerchantNo(), "-"));
            }
        }
    }

    public void handleEvent(Long id, String action, String handleNote, Boolean addBlacklist) {
        RiskEvent event = getEventById(id);
        if ("PROCESSED".equals(action)) {
            event.setStatus(1);
        } else if ("IGNORED".equals(action)) {
            event.setStatus(2);
        } else {
            event.setStatus(1);
        }
        event.setHandleNote(handleNote);
        event.setHandledAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        riskEventMapper.updateById(event);

        if (Boolean.TRUE.equals(addBlacklist) && StringUtils.hasText(event.getMerchantNo())) {
            RiskBlacklist blacklist = new RiskBlacklist();
            blacklist.setType("MERCHANT");
            blacklist.setValue(event.getMerchantNo());
            blacklist.setReason("风险事件自动加入黑名单: " + (StringUtils.hasText(handleNote) ? handleNote : event.getTriggerRule()));
            blacklist.setStatus(1);
            blacklist.setCreatedAt(LocalDateTime.now());
            blacklist.setUpdatedAt(LocalDateTime.now());
            blacklist.setDeleted(0);
            riskBlacklistMapper.insert(blacklist);
        }
    }

    public Map<String, Object> getEventStats() {
        Map<String, Object> stats = new HashMap<>();
        Long pendingCount = riskEventMapper.selectCount(
                new LambdaQueryWrapper<RiskEvent>()
                        .eq(RiskEvent::getDeleted, 0)
                        .eq(RiskEvent::getStatus, 0)
        );
        Long highCount = riskEventMapper.selectCount(
                new LambdaQueryWrapper<RiskEvent>()
                        .eq(RiskEvent::getDeleted, 0)
                        .eq(RiskEvent::getRiskLevel, "HIGH")
                        .eq(RiskEvent::getStatus, 0)
        );
        Long mediumCount = riskEventMapper.selectCount(
                new LambdaQueryWrapper<RiskEvent>()
                        .eq(RiskEvent::getDeleted, 0)
                        .eq(RiskEvent::getRiskLevel, "MEDIUM")
                        .eq(RiskEvent::getStatus, 0)
        );
        Long lowCount = riskEventMapper.selectCount(
                new LambdaQueryWrapper<RiskEvent>()
                        .eq(RiskEvent::getDeleted, 0)
                        .eq(RiskEvent::getRiskLevel, "LOW")
                        .eq(RiskEvent::getStatus, 0)
        );
        Long todayCount = riskEventMapper.selectCount(
                new LambdaQueryWrapper<RiskEvent>()
                        .eq(RiskEvent::getDeleted, 0)
                        .ge(RiskEvent::getTriggeredAt, LocalDateTime.now().withHour(0).withMinute(0).withSecond(0))
        );
        Long totalCount = riskEventMapper.selectCount(
                new LambdaQueryWrapper<RiskEvent>().eq(RiskEvent::getDeleted, 0)
        );
        stats.put("highCount", highCount);
        stats.put("mediumCount", mediumCount);
        stats.put("lowCount", lowCount);
        stats.put("pendingCount", pendingCount);
        stats.put("highRiskCount", highCount);
        stats.put("todayCount", todayCount);
        stats.put("totalCount", totalCount);
        return stats;
    }

    public PageResult<RiskBlacklist> listBlacklist(PageQuery pageQuery, String type) {
        Page<RiskBlacklist> page = new Page<>(pageQuery.getPage(), pageQuery.getPageSize());
        LambdaQueryWrapper<RiskBlacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RiskBlacklist::getDeleted, 0);
        if (StringUtils.hasText(type)) {
            wrapper.eq(RiskBlacklist::getType, type);
        }
        wrapper.orderByDesc(RiskBlacklist::getCreatedAt);
        Page<RiskBlacklist> result = riskBlacklistMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), pageQuery.getPage(), pageQuery.getPageSize());
    }

    public RiskBlacklist addBlacklist(RiskBlacklist blacklist) {
        blacklist.setCreatedAt(LocalDateTime.now());
        blacklist.setUpdatedAt(LocalDateTime.now());
        blacklist.setDeleted(0);
        if (blacklist.getStatus() == null) blacklist.setStatus(1);
        riskBlacklistMapper.insert(blacklist);
        return blacklist;
    }

    public void removeBlacklist(Long id) {
        RiskBlacklist blacklist = riskBlacklistMapper.selectOne(
                new LambdaQueryWrapper<RiskBlacklist>()
                        .eq(RiskBlacklist::getId, id)
                        .eq(RiskBlacklist::getDeleted, 0)
        );
        if (blacklist == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        blacklist.setDeleted(1);
        blacklist.setUpdatedAt(LocalDateTime.now());
        riskBlacklistMapper.updateById(blacklist);
    }
}
