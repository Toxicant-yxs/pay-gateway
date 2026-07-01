package com.paygateway.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paygateway.admin.entity.ChannelConfig;
import com.paygateway.admin.mapper.ChannelConfigMapper;
import com.paygateway.common.exception.BusinessException;
import com.paygateway.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelConfigMapper channelConfigMapper;

    public List<ChannelConfig> list(String keyword, Integer status) {
        LambdaQueryWrapper<ChannelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChannelConfig::getDeleted, 0);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ChannelConfig::getChannelName, keyword)
                    .or().like(ChannelConfig::getChannelCode, keyword));
        }
        if (status != null) {
            wrapper.eq(ChannelConfig::getStatus, status);
        }
        wrapper.orderByAsc(ChannelConfig::getPriority);
        return channelConfigMapper.selectList(wrapper);
    }

    public ChannelConfig getById(Long id) {
        ChannelConfig channel = channelConfigMapper.selectOne(
                new LambdaQueryWrapper<ChannelConfig>()
                        .eq(ChannelConfig::getId, id)
                        .eq(ChannelConfig::getDeleted, 0)
        );
        if (channel == null) {
            throw new BusinessException(ResultCode.CHANNEL_NOT_EXIST);
        }
        return channel;
    }

    public ChannelConfig create(ChannelConfig channel) {
        channel.setCreatedAt(LocalDateTime.now());
        channel.setUpdatedAt(LocalDateTime.now());
        channel.setDeleted(0);
        if (channel.getStatus() == null) channel.setStatus(1);
        if (channel.getPriority() == null) channel.setPriority(100);
        channelConfigMapper.insert(channel);
        return channel;
    }

    public ChannelConfig update(Long id, ChannelConfig channel) {
        ChannelConfig existing = getById(id);
        if (StringUtils.hasText(channel.getChannelName())) existing.setChannelName(channel.getChannelName());
        if (StringUtils.hasText(channel.getChannelCode())) existing.setChannelCode(channel.getChannelCode());
        if (channel.getChannelType() != null) existing.setChannelType(channel.getChannelType());
        if (channel.getMchId() != null) existing.setMchId(channel.getMchId());
        if (channel.getAppId() != null) existing.setAppId(channel.getAppId());
        if (channel.getApiUrl() != null) existing.setApiUrl(channel.getApiUrl());
        if (channel.getFeeRate() != null) existing.setFeeRate(channel.getFeeRate());
        if (channel.getPriority() != null) existing.setPriority(channel.getPriority());
        if (channel.getStatus() != null) existing.setStatus(channel.getStatus());
        if (channel.getRemark() != null) existing.setRemark(channel.getRemark());
        existing.setUpdatedAt(LocalDateTime.now());
        channelConfigMapper.updateById(existing);
        return existing;
    }

    public void updateStatus(Long id, Integer status) {
        ChannelConfig channel = getById(id);
        channel.setStatus(status);
        channel.setUpdatedAt(LocalDateTime.now());
        channelConfigMapper.updateById(channel);
    }
}
