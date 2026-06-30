import request from '@/utils/request'
import type { ReconTask, ReconDetail, ReconStats, ReconQuery, ReconReport } from '@/types/reconciliation'
import type { PageResult } from '@/types/api'

export const reconApi = {
  getTasks(params: ReconQuery) {
    return request<PageResult<ReconTask>>({
      url: '/reconciliation/tasks',
      method: 'get',
      params
    })
  },

  getTaskDetail(taskId: string) {
    return request<ReconTask>({
      url: `/reconciliation/tasks/${taskId}`,
      method: 'get'
    })
  },

  getTaskDiffs(taskId: string, params?: { page?: number; pageSize?: number; diffType?: string }) {
    return request<PageResult<ReconDetail>>({
      url: `/reconciliation/tasks/${taskId}/diffs`,
      method: 'get',
      params
    })
  },

  handleDiff(diffId: string, data: { action: string; note?: string }) {
    return request({
      url: `/reconciliation/diffs/${diffId}/handle`,
      method: 'post',
      data
    })
  },

  retryTask(taskId: string) {
    return request({
      url: `/reconciliation/tasks/${taskId}/retry`,
      method: 'post'
    })
  },

  downloadBill(taskId: string) {
    return request<Blob>({
      url: `/reconciliation/tasks/${taskId}/download`,
      method: 'get',
      responseType: 'blob'
    })
  },

  getStats() {
    return request<ReconStats>({
      url: '/reconciliation/tasks/stats',
      method: 'get'
    })
  },

  getReports(params?: { startDate?: string; endDate?: string; channelCode?: string; page?: number; pageSize?: number }) {
    return request<PageResult<ReconReport>>({
      url: '/reconciliation/reports',
      method: 'get',
      params
    })
  },

  exportReport(params: { startDate?: string; endDate?: string; channelCode?: string; format?: 'excel' | 'pdf' }) {
    return request<Blob>({
      url: '/reconciliation/reports/export',
      method: 'get',
      params,
      responseType: 'blob'
    })
  }
}
