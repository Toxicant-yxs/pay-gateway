import request from '@/utils/request'
import type {
  MerchantItem,
  MerchantQuery,
  MerchantAuditItem,
  AuditStats,
  MerchantStatusCount
} from '@/types/merchant'
import type { PageResult } from '@/types/api'

export const merchantApi = {
  getList(params: MerchantQuery) {
    return request<PageResult<MerchantItem>>({
      url: '/merchants',
      method: 'get',
      params
    })
  },

  getDetail(merchantId: string) {
    return request<MerchantItem>({
      url: `/merchants/${merchantId}`,
      method: 'get'
    })
  },

  create(data: Partial<MerchantItem>) {
    return request({
      url: '/merchants',
      method: 'post',
      data
    })
  },

  update(merchantId: string, data: Partial<MerchantItem>) {
    return request({
      url: `/merchants/${merchantId}`,
      method: 'put',
      data
    })
  },

  updateStatus(merchantId: string, data: { status: string; reason?: string }) {
    return request({
      url: `/merchants/${merchantId}/status`,
      method: 'put',
      data
    })
  },

  getAuditList(params?: MerchantQuery) {
    return request<PageResult<MerchantAuditItem>>({
      url: '/merchants/audits',
      method: 'get',
      params
    })
  },

  approve(auditId: string) {
    return request({
      url: `/merchants/audits/${auditId}/approve`,
      method: 'post'
    })
  },

  reject(auditId: string, data: { reason: string }) {
    return request({
      url: `/merchants/audits/${auditId}/reject`,
      method: 'post',
      data
    })
  },

  getAuditStats() {
    return request<AuditStats>({
      url: '/merchants/audits/stats',
      method: 'get'
    })
  },

  getStatusCount() {
    return request<MerchantStatusCount>({
      url: '/merchants/status-count',
      method: 'get'
    })
  }
}
