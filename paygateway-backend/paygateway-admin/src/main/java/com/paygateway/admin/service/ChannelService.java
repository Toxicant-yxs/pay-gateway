package com.paygateway.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paygateway.admin.entity.ChannelConfig;
import com.paygateway.admin.mapper.ChannelConfigMapper;
import com.paygateway.common.exception.BusinessException;
import com.paygateway.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelConfigMapper channelConfigMapper;

    public List<ChannelConfig> list() {
        return channelConfigMapper.selectList(
                new LambdaQueryWrapper<ChannelConfig>()
                        .eq(ChannelConfig::getDeleted, 0)
                        .orderByAsc(ChannelConfig::getPriority)
        );
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

    public void updateStatus(Long id, Integer status) {
        ChannelConfig channel = getById(id);
        channel.setStatus(status);
        channel.setUpdatedAt(LocalDateTime.now());
        channelConfigMapper.updateById(channel);
    }
}
