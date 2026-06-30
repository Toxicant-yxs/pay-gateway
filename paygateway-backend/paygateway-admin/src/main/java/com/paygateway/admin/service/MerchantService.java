package com.paygateway.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paygateway.admin.entity.MerchantInfo;
import com.paygateway.admin.mapper.MerchantInfoMapper;
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
public class MerchantService {

    private final MerchantInfoMapper merchantInfoMapper;

    public PageResult<MerchantInfo> list(PageQuery pageQuery, String keyword, Integer status) {
        Page<MerchantInfo> page = new Page<>(pageQuery.getPage(), pageQuery.getPageSize());

        LambdaQueryWrapper<MerchantInfo> wrapper = new LambdaQueryWrapper<MerchantInfo>()
                .eq(MerchantInfo::getDeleted, 0)
                .orderByDesc(MerchantInfo::getCreatedAt);

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(MerchantInfo::getMerchantName, keyword)
                    .or().like(MerchantInfo::getMerchantNo, keyword)
                    .or().like(MerchantInfo::getShortName, keyword));
        }

        if (status != null) {
            wrapper.eq(MerchantInfo::getStatus, status);
        }

        Page<MerchantInfo> result = merchantInfoMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), pageQuery.getPage(), pageQuery.getPageSize());
    }

    public MerchantInfo getById(Long id) {
        MerchantInfo merchant = merchantInfoMapper.selectOne(
                new LambdaQueryWrapper<MerchantInfo>()
                        .eq(MerchantInfo::getId, id)
                        .eq(MerchantInfo::getDeleted, 0)
        );
        if (merchant == null) {
            throw new BusinessException(ResultCode.MERCHANT_NOT_EXIST);
        }
        return merchant;
    }

    public void audit(Long id, Integer status, String remark) {
        MerchantInfo merchant = getById(id);
        merchant.setStatus(status);
        merchant.setAuditRemark(remark);
        merchant.setAuditTime(LocalDateTime.now());
        merchant.setUpdatedAt(LocalDateTime.now());
        merchantInfoMapper.updateById(merchant);
    }
}
