package com.paygateway.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paygateway.admin.entity.ReconDetail;
import com.paygateway.admin.entity.ReconTask;
import com.paygateway.admin.mapper.ReconDetailMapper;
import com.paygateway.admin.mapper.ReconTaskMapper;
import com.paygateway.common.dto.PageQuery;
import com.paygateway.common.exception.BusinessException;
import com.paygateway.common.result.PageResult;
import com.paygateway.common.result.ResultCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReconService {

    private final ReconTaskMapper reconTaskMapper;
    private final ReconDetailMapper reconDetailMapper;

    public PageResult<ReconTask> listTasks(PageQuery pageQuery, String channelCode, Integer status,
                                            LocalDate startDate, LocalDate endDate) {
        Page<ReconTask> page = new Page<>(pageQuery.getPage(), pageQuery.getPageSize());
        LambdaQueryWrapper<ReconTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReconTask::getDeleted, 0);
        if (StringUtils.hasText(channelCode)) {
            wrapper.eq(ReconTask::getChannelCode, channelCode);
        }
        if (status != null) {
            wrapper.eq(ReconTask::getStatus, status);
        }
        if (startDate != null) {
            wrapper.ge(ReconTask::getReconDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(ReconTask::getReconDate, endDate);
        }
        wrapper.orderByDesc(ReconTask::getReconDate);
        Page<ReconTask> result = reconTaskMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), pageQuery.getPage(), pageQuery.getPageSize());
    }

    public ReconTask getTaskById(Long id) {
        ReconTask task = reconTaskMapper.selectOne(
                new LambdaQueryWrapper<ReconTask>()
                        .eq(ReconTask::getId, id)
                        .eq(ReconTask::getDeleted, 0)
        );
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return task;
    }

    public PageResult<ReconDetail> getTaskDiffs(Long taskId, PageQuery pageQuery, String diffType) {
        Page<ReconDetail> page = new Page<>(pageQuery.getPage(), pageQuery.getPageSize());
        LambdaQueryWrapper<ReconDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReconDetail::getDeleted, 0)
                .eq(ReconDetail::getTaskId, taskId);
        if (StringUtils.hasText(diffType)) {
            wrapper.eq(ReconDetail::getDiffType, diffType);
        }
        wrapper.orderByDesc(ReconDetail::getCreatedAt);
        Page<ReconDetail> result = reconDetailMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), pageQuery.getPage(), pageQuery.getPageSize());
    }

    public void handleDiff(Long diffId, String action, String note) {
        ReconDetail detail = reconDetailMapper.selectOne(
                new LambdaQueryWrapper<ReconDetail>()
                        .eq(ReconDetail::getId, diffId)
                        .eq(ReconDetail::getDeleted, 0)
        );
        if (detail == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if ("RESOLVE".equals(action)) {
            detail.setStatus(1);
        } else if ("IGNORE".equals(action)) {
            detail.setStatus(2);
        }
        detail.setHandleNote(note);
        detail.setHandledAt(LocalDateTime.now());
        detail.setUpdatedAt(LocalDateTime.now());
        reconDetailMapper.updateById(detail);
    }

    public void retryTask(Long taskId) {
        ReconTask task = getTaskById(taskId);
        task.setStatus(0);
        task.setErrorMsg(null);
        task.setStartedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        reconTaskMapper.updateById(task);
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        Long pendingCount = reconTaskMapper.selectCount(
                new LambdaQueryWrapper<ReconTask>()
                        .eq(ReconTask::getDeleted, 0)
                        .eq(ReconTask::getStatus, 0)
        );
        Long processingCount = reconTaskMapper.selectCount(
                new LambdaQueryWrapper<ReconTask>()
                        .eq(ReconTask::getDeleted, 0)
                        .eq(ReconTask::getStatus, 1)
        );
        Long successCount = reconTaskMapper.selectCount(
                new LambdaQueryWrapper<ReconTask>()
                        .eq(ReconTask::getDeleted, 0)
                        .eq(ReconTask::getStatus, 2)
        );
        Long failCount = reconTaskMapper.selectCount(
                new LambdaQueryWrapper<ReconTask>()
                        .eq(ReconTask::getDeleted, 0)
                        .eq(ReconTask::getStatus, 3)
        );
        stats.put("pendingCount", pendingCount);
        stats.put("processingCount", processingCount);
        stats.put("successCount", successCount);
        stats.put("failCount", failCount);
        stats.put("totalTasks", pendingCount + processingCount + successCount + failCount);
        return stats;
    }

    public PageResult<Map<String, Object>> getReports(PageQuery pageQuery, LocalDate startDate, LocalDate endDate, String channelCode) {
        List<ReconTask> tasks = reconTaskMapper.selectList(
                new LambdaQueryWrapper<ReconTask>()
                        .eq(ReconTask::getDeleted, 0)
                        .eq(ReconTask::getStatus, 2)
                        .ge(startDate != null, ReconTask::getReconDate, startDate)
                        .le(endDate != null, ReconTask::getReconDate, endDate)
                        .eq(StringUtils.hasText(channelCode), ReconTask::getChannelCode, channelCode)
                        .orderByDesc(ReconTask::getReconDate)
        );
        int fromIndex = (int) ((pageQuery.getPage() - 1) * pageQuery.getPageSize());
        int toIndex = Math.min(fromIndex + pageQuery.getPageSize(), tasks.size());
        List<ReconTask> pageList = tasks.subList(Math.max(fromIndex, 0), Math.max(toIndex, fromIndex));
        return PageResult.of(
                pageList.stream().map(t -> {
                    Map<String, Object> report = new HashMap<>();
                    report.put("reconDate", t.getReconDate());
                    report.put("channelCode", t.getChannelCode());
                    report.put("totalCount", t.getTotalCount());
                    report.put("totalAmount", t.getTotalAmount());
                    report.put("matchCount", t.getMatchCount());
                    report.put("diffCount", t.getDiffCount());
                    report.put("successRate", t.getTotalCount() > 0 ?
                            String.format("%.2f%%", (double) t.getMatchCount() / t.getTotalCount() * 100) : "0%");
                    return report;
                }).toList(),
                (long) tasks.size(), pageQuery.getPage(), pageQuery.getPageSize()
        );
    }

    public void downloadBill(Long taskId, HttpServletResponse response) throws IOException {
        ReconTask task = getTaskById(taskId);
        List<ReconDetail> details = reconDetailMapper.selectList(
                new LambdaQueryWrapper<ReconDetail>()
                        .eq(ReconDetail::getDeleted, 0)
                        .eq(ReconDetail::getTaskId, taskId)
        );

        response.setContentType("text/csv;charset=UTF-8");
        String fileName = URLEncoder.encode("reconciliation_bill_" + taskId + ".csv", StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        PrintWriter writer = response.getWriter();
        writer.println("订单号,我方金额,通道金额,我方状态,通道状态,差异类型,处理状态,处理备注,创建时间");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (ReconDetail detail : details) {
            writer.printf("%s,%s,%s,%d,%d,%s,%d,%s,%s%n",
                    detail.getOrderNo() != null ? detail.getOrderNo() : "",
                    detail.getOurAmount() != null ? detail.getOurAmount().toString() : "0",
                    detail.getChannelAmount() != null ? detail.getChannelAmount().toString() : "0",
                    detail.getOurStatus() != null ? detail.getOurStatus() : 0,
                    detail.getChannelStatus() != null ? detail.getChannelStatus() : 0,
                    detail.getDiffType() != null ? detail.getDiffType() : "",
                    detail.getStatus() != null ? detail.getStatus() : 0,
                    detail.getHandleNote() != null ? detail.getHandleNote() : "",
                    detail.getCreatedAt() != null ? detail.getCreatedAt().format(fmt) : ""
            );
        }
        writer.flush();
        writer.close();
    }

    public void exportReport(LocalDate startDate, LocalDate endDate, String channelCode, String format, HttpServletResponse response) throws IOException {
        List<ReconTask> tasks = reconTaskMapper.selectList(
                new LambdaQueryWrapper<ReconTask>()
                        .eq(ReconTask::getDeleted, 0)
                        .eq(ReconTask::getStatus, 2)
                        .ge(startDate != null, ReconTask::getReconDate, startDate)
                        .le(endDate != null, ReconTask::getReconDate, endDate)
                        .eq(StringUtils.hasText(channelCode), ReconTask::getChannelCode, channelCode)
                        .orderByDesc(ReconTask::getReconDate)
        );

        response.setContentType("text/csv;charset=UTF-8");
        String fileName = URLEncoder.encode("reconciliation_report.csv", StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        PrintWriter writer = response.getWriter();
        writer.println("对账日期,通道编码,交易总数,交易总金额,匹配数量,差异数量,成功率");
        for (ReconTask task : tasks) {
            double successRate = task.getTotalCount() != null && task.getTotalCount() > 0 ?
                    (double) task.getMatchCount() / task.getTotalCount() * 100 : 0;
            writer.printf("%s,%s,%d,%s,%d,%d,%.2f%%%n",
                    task.getReconDate() != null ? task.getReconDate().toString() : "",
                    task.getChannelCode() != null ? task.getChannelCode() : "",
                    task.getTotalCount() != null ? task.getTotalCount() : 0,
                    task.getTotalAmount() != null ? task.getTotalAmount().toString() : "0",
                    task.getMatchCount() != null ? task.getMatchCount() : 0,
                    task.getDiffCount() != null ? task.getDiffCount() : 0,
                    successRate
            );
        }
        writer.flush();
        writer.close();
    }
}
