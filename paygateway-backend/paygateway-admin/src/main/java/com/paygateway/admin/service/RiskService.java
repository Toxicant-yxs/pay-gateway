package com.paygateway.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paygateway.admin.entity.RiskEvent;
import com.paygateway.admin.entity.RiskRule;
import com.paygateway.admin.mapper.RiskEventMapper;
import com.paygateway.admin.mapper.RiskRuleMapper;
import com.paygateway.common.dto.PageQuery;
import com.paygateway.common.exception.BusinessException;
import com.paygateway.common.result.PageResult;
import com.paygateway.common.result.ResultCode;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RiskService {

    private final RiskRuleMapper riskRuleMapper;
    private final RiskEventMapper riskEventMapper;

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
        return event;
    }

    public void handleEvent(Long id, String handleNote) {
        RiskEvent event = getEventById(id);
        event.setStatus(1);
        event.setHandleNote(handleNote);
        event.setHandledAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        riskEventMapper.updateById(event);
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
        Long todayCount = riskEventMapper.selectCount(
                new LambdaQueryWrapper<RiskEvent>()
                        .eq(RiskEvent::getDeleted, 0)
                        .ge(RiskEvent::getTriggeredAt, LocalDateTime.now().withHour(0).withMinute(0).withSecond(0))
        );
        Long totalCount = riskEventMapper.selectCount(
                new LambdaQueryWrapper<RiskEvent>().eq(RiskEvent::getDeleted, 0)
        );
        stats.put("pendingCount", pendingCount);
        stats.put("highRiskCount", highCount);
        stats.put("todayCount", todayCount);
        stats.put("totalCount", totalCount);
        return stats;
    }
}
