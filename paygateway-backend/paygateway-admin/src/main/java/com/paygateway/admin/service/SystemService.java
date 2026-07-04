package com.paygateway.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paygateway.admin.entity.OperationLog;
import com.paygateway.admin.entity.SystemConfig;
import com.paygateway.admin.entity.SystemNotification;
import com.paygateway.admin.mapper.OperationLogMapper;
import com.paygateway.admin.mapper.SystemConfigMapper;
import com.paygateway.admin.mapper.SystemNotificationMapper;
import com.paygateway.common.dto.PageQuery;
import com.paygateway.common.exception.BusinessException;
import com.paygateway.common.result.PageResult;
import com.paygateway.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemService {

    private final SystemConfigMapper systemConfigMapper;
    private final SystemNotificationMapper notificationMapper;
    private final OperationLogMapper operationLogMapper;

    public Map<String, Object> getConfig() {
        List<SystemConfig> configs = systemConfigMapper.selectList(null);
        Map<String, Object> result = new HashMap<>();
        Map<String, Map<String, Object>> groups = new HashMap<>();
        for (SystemConfig config : configs) {
            groups.computeIfAbsent(config.getConfigGroup(), k -> new HashMap<>())
                    .put(config.getConfigKey(), config.getConfigValue());
        }
        result.put("groups", groups);
        result.put("list", configs);
        return result;
    }

    public void updateConfig(List<SystemConfig> configs) {
        for (SystemConfig config : configs) {
            SystemConfig existing = systemConfigMapper.selectOne(
                    new LambdaQueryWrapper<SystemConfig>()
                            .eq(SystemConfig::getConfigKey, config.getConfigKey())
            );
            if (existing != null) {
                existing.setConfigValue(config.getConfigValue());
                existing.setUpdatedAt(LocalDateTime.now());
                systemConfigMapper.updateById(existing);
            } else {
                config.setCreatedAt(LocalDateTime.now());
                config.setUpdatedAt(LocalDateTime.now());
                systemConfigMapper.insert(config);
            }
        }
    }

    public Long getUnreadCount() {
        return notificationMapper.selectCount(
                new LambdaQueryWrapper<SystemNotification>()
                        .eq(SystemNotification::getDeleted, 0)
                        .eq(SystemNotification::getIsRead, 0)
        );
    }

    public PageResult<SystemNotification> getNotifications(PageQuery pageQuery, String category) {
        Page<SystemNotification> page = new Page<>(pageQuery.getPage(), pageQuery.getPageSize());
        LambdaQueryWrapper<SystemNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemNotification::getDeleted, 0);
        if (StringUtils.hasText(category)) {
            wrapper.eq(SystemNotification::getCategory, category);
        }
        wrapper.orderByDesc(SystemNotification::getCreatedAt);
        Page<SystemNotification> result = notificationMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), pageQuery.getPage(), pageQuery.getPageSize());
    }

    public void markRead(Long id) {
        SystemNotification notify = notificationMapper.selectOne(
                new LambdaQueryWrapper<SystemNotification>()
                        .eq(SystemNotification::getId, id)
                        .eq(SystemNotification::getDeleted, 0)
        );
        if (notify == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        notify.setIsRead(1);
        notify.setReadAt(LocalDateTime.now());
        notify.setUpdatedAt(LocalDateTime.now());
        notificationMapper.updateById(notify);
    }

    public void markAllRead() {
        List<SystemNotification> unreadList = notificationMapper.selectList(
                new LambdaQueryWrapper<SystemNotification>()
                        .eq(SystemNotification::getDeleted, 0)
                        .eq(SystemNotification::getIsRead, 0)
        );
        LocalDateTime now = LocalDateTime.now();
        for (SystemNotification notify : unreadList) {
            notify.setIsRead(1);
            notify.setReadAt(now);
            notify.setUpdatedAt(now);
            notificationMapper.updateById(notify);
        }
    }

    public PageResult<OperationLog> getOperationLogs(PageQuery pageQuery, String username,
                                                       LocalDateTime startTime, LocalDateTime endTime) {
        Page<OperationLog> page = new Page<>(pageQuery.getPage(), pageQuery.getPageSize());
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(OperationLog::getUsername, username);
        }
        if (startTime != null) {
            wrapper.ge(OperationLog::getCreatedAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(OperationLog::getCreatedAt, endTime);
        }
        wrapper.orderByDesc(OperationLog::getCreatedAt);
        Page<OperationLog> result = operationLogMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), pageQuery.getPage(), pageQuery.getPageSize());
    }

    public Map<String, Object> globalSearch(String keyword, String types) {
        Map<String, Object> result = new HashMap<>();
        return result;
    }
}
