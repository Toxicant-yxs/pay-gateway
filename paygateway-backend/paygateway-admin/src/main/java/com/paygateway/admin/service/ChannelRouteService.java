package com.paygateway.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paygateway.admin.entity.ChannelRoute;
import com.paygateway.admin.mapper.ChannelRouteMapper;
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
public class ChannelRouteService {

    private final ChannelRouteMapper channelRouteMapper;

    public PageResult<ChannelRoute> list(PageQuery pageQuery, Integer status) {
        Page<ChannelRoute> page = new Page<>(pageQuery.getPage(), pageQuery.getPageSize());
        LambdaQueryWrapper<ChannelRoute> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChannelRoute::getDeleted, 0);
        if (status != null) {
            wrapper.eq(ChannelRoute::getStatus, status);
        }
        wrapper.orderByAsc(ChannelRoute::getPriority);
        Page<ChannelRoute> result = channelRouteMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), pageQuery.getPage(), pageQuery.getPageSize());
    }

    public ChannelRoute getById(Long id) {
        ChannelRoute route = channelRouteMapper.selectOne(
                new LambdaQueryWrapper<ChannelRoute>()
                        .eq(ChannelRoute::getId, id)
                        .eq(ChannelRoute::getDeleted, 0)
        );
        if (route == null) {
            throw new BusinessException(ResultCode.CHANNEL_NOT_EXIST);
        }
        return route;
    }

    public ChannelRoute create(ChannelRoute route) {
        route.setRouteNo("ROUTE" + System.currentTimeMillis());
        route.setCreatedAt(LocalDateTime.now());
        route.setUpdatedAt(LocalDateTime.now());
        route.setDeleted(0);
        if (route.getStatus() == null) route.setStatus(1);
        if (route.getPriority() == null) route.setPriority(100);
        channelRouteMapper.insert(route);
        return route;
    }

    public ChannelRoute update(Long id, ChannelRoute route) {
        ChannelRoute existing = getById(id);
        if (StringUtils.hasText(route.getRouteName())) existing.setRouteName(route.getRouteName());
        if (route.getChannelId() != null) existing.setChannelId(route.getChannelId());
        if (StringUtils.hasText(route.getChannelCode())) existing.setChannelCode(route.getChannelCode());
        if (StringUtils.hasText(route.getPayType())) existing.setPayType(route.getPayType());
        if (route.getMinAmount() != null) existing.setMinAmount(route.getMinAmount());
        if (route.getMaxAmount() != null) existing.setMaxAmount(route.getMaxAmount());
        if (route.getPriority() != null) existing.setPriority(route.getPriority());
        if (route.getStatus() != null) existing.setStatus(route.getStatus());
        if (route.getTimeStart() != null) existing.setTimeStart(route.getTimeStart());
        if (route.getTimeEnd() != null) existing.setTimeEnd(route.getTimeEnd());
        if (route.getRemark() != null) existing.setRemark(route.getRemark());
        existing.setUpdatedAt(LocalDateTime.now());
        channelRouteMapper.updateById(existing);
        return existing;
    }

    public void delete(Long id) {
        ChannelRoute route = getById(id);
        route.setDeleted(1);
        route.setUpdatedAt(LocalDateTime.now());
        channelRouteMapper.updateById(route);
    }
}
