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
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private static final DateTimeFormatter MERCHANT_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final MerchantInfoMapper merchantInfoMapper;

    public PageResult<MerchantInfo> list(PageQuery pageQuery, String keyword, Integer status) {
        return list(pageQuery, keyword, null, null, status, null, null);
    }

    public PageResult<MerchantInfo> list(PageQuery pageQuery, String merchantName, String merchantNo, String keyword, Integer status, LocalDateTime startTime, LocalDateTime endTime) {
        Page<MerchantInfo> page = new Page<>(pageQuery.getPage(), pageQuery.getPageSize());

        LambdaQueryWrapper<MerchantInfo> wrapper = new LambdaQueryWrapper<MerchantInfo>()
                .eq(MerchantInfo::getDeleted, 0)
                .orderByDesc(MerchantInfo::getCreatedAt);

        if (StringUtils.hasText(merchantName)) {
            wrapper.like(MerchantInfo::getMerchantName, merchantName);
        }
        if (StringUtils.hasText(merchantNo)) {
            wrapper.like(MerchantInfo::getMerchantNo, merchantNo);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(MerchantInfo::getMerchantName, keyword)
                    .or().like(MerchantInfo::getMerchantNo, keyword)
                    .or().like(MerchantInfo::getShortName, keyword));
        }
        if (status != null) {
            wrapper.eq(MerchantInfo::getStatus, status);
        }
        if (startTime != null) {
            wrapper.ge(MerchantInfo::getCreatedAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(MerchantInfo::getCreatedAt, endTime);
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

    public MerchantInfo create(MerchantInfo merchant) {
        if (!StringUtils.hasText(merchant.getMerchantNo())) {
            merchant.setMerchantNo(generateMerchantNo());
        }
        if (merchant.getStatus() == null) {
            merchant.setStatus(0);
        }
        if (merchant.getLevel() == null) {
            merchant.setLevel(1);
        }
        if (merchant.getFeeRate() == null) {
            merchant.setFeeRate(new java.math.BigDecimal("0.006"));
        }
        LocalDateTime now = LocalDateTime.now();
        merchant.setCreatedAt(now);
        merchant.setUpdatedAt(now);
        merchant.setDeleted(0);
        merchantInfoMapper.insert(merchant);
        return merchant;
    }

    public MerchantInfo update(Long id, MerchantInfo merchant) {
        MerchantInfo existing = getById(id);
        if (StringUtils.hasText(merchant.getMerchantName())) {
            existing.setMerchantName(merchant.getMerchantName());
        }
        if (StringUtils.hasText(merchant.getShortName())) {
            existing.setShortName(merchant.getShortName());
        }
        if (StringUtils.hasText(merchant.getIndustry())) {
            existing.setIndustry(merchant.getIndustry());
        }
        if (StringUtils.hasText(merchant.getContactName())) {
            existing.setContactName(merchant.getContactName());
        }
        if (StringUtils.hasText(merchant.getContactPhone())) {
            existing.setContactPhone(merchant.getContactPhone());
        }
        if (StringUtils.hasText(merchant.getContactEmail())) {
            existing.setContactEmail(merchant.getContactEmail());
        }
        if (merchant.getLevel() != null) {
            existing.setLevel(merchant.getLevel());
        }
        if (merchant.getFeeRate() != null) {
            existing.setFeeRate(merchant.getFeeRate());
        }
        if (StringUtils.hasText(merchant.getRemark())) {
            existing.setRemark(merchant.getRemark());
        }
        existing.setUpdatedAt(LocalDateTime.now());
        merchantInfoMapper.updateById(existing);
        return existing;
    }

    public void updateStatus(Long id, Integer status, String reason) {
        MerchantInfo merchant = getById(id);
        merchant.setStatus(status);
        if (StringUtils.hasText(reason)) {
            merchant.setAuditRemark(reason);
        }
        merchant.setUpdatedAt(LocalDateTime.now());
        merchantInfoMapper.updateById(merchant);
    }

    public void audit(Long id, Integer status, String remark) {
        MerchantInfo merchant = getById(id);
        merchant.setStatus(status);
        merchant.setAuditRemark(remark);
        merchant.setAuditTime(LocalDateTime.now());
        merchant.setUpdatedAt(LocalDateTime.now());
        merchantInfoMapper.updateById(merchant);
    }

    public PageResult<MerchantInfo> getAuditList(PageQuery pageQuery) {
        Page<MerchantInfo> page = new Page<>(pageQuery.getPage(), pageQuery.getPageSize());
        LambdaQueryWrapper<MerchantInfo> wrapper = new LambdaQueryWrapper<MerchantInfo>()
                .eq(MerchantInfo::getDeleted, 0)
                .eq(MerchantInfo::getStatus, 0)
                .orderByDesc(MerchantInfo::getCreatedAt);
        Page<MerchantInfo> result = merchantInfoMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), pageQuery.getPage(), pageQuery.getPageSize());
    }

    public void approve(Long id) {
        audit(id, 1, "审核通过");
    }

    public void reject(Long id, String reason) {
        audit(id, 4, reason);
    }

    public Map<String, Object> getAuditStats() {
        Map<String, Object> stats = new HashMap<>();
        Long pendingCount = merchantInfoMapper.selectCount(
                new LambdaQueryWrapper<MerchantInfo>()
                        .eq(MerchantInfo::getDeleted, 0)
                        .eq(MerchantInfo::getStatus, 0)
        );
        stats.put("pendingCount", pendingCount);
        stats.put("todayCount", merchantInfoMapper.selectCount(
                new LambdaQueryWrapper<MerchantInfo>()
                        .eq(MerchantInfo::getDeleted, 0)
                        .ge(MerchantInfo::getCreatedAt, LocalDateTime.now().toLocalDate().atStartOfDay())
        ));
        return stats;
    }

    public Map<String, Object> getStatusCount() {
        Map<String, Object> counts = new HashMap<>();
        counts.put("pending", merchantInfoMapper.selectCount(
                new LambdaQueryWrapper<MerchantInfo>().eq(MerchantInfo::getDeleted, 0).eq(MerchantInfo::getStatus, 0)));
        counts.put("normal", merchantInfoMapper.selectCount(
                new LambdaQueryWrapper<MerchantInfo>().eq(MerchantInfo::getDeleted, 0).eq(MerchantInfo::getStatus, 1)));
        counts.put("frozen", merchantInfoMapper.selectCount(
                new LambdaQueryWrapper<MerchantInfo>().eq(MerchantInfo::getDeleted, 0).eq(MerchantInfo::getStatus, 2)));
        counts.put("disabled", merchantInfoMapper.selectCount(
                new LambdaQueryWrapper<MerchantInfo>().eq(MerchantInfo::getDeleted, 0).eq(MerchantInfo::getStatus, 3)));
        counts.put("rejected", merchantInfoMapper.selectCount(
                new LambdaQueryWrapper<MerchantInfo>().eq(MerchantInfo::getDeleted, 0).eq(MerchantInfo::getStatus, 4)));
        Long total = merchantInfoMapper.selectCount(
                new LambdaQueryWrapper<MerchantInfo>().eq(MerchantInfo::getDeleted, 0));
        counts.put("total", total);
        return counts;
    }

    private String generateMerchantNo() {
        return "M" + LocalDateTime.now().format(MERCHANT_NO_FMT) + String.format("%03d", (int)(Math.random() * 1000));
    }
}
