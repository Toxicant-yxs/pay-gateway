import request from '@/utils/request'
import type { TradeOrder, TradeQuery, RefundOrder, RefundQuery, RefundRequest, TradeStatistics } from '@/types/transaction'
import type { PageResult } from '@/types/api'

export const tradeApi = {
  getStatistics(params?: { startTime?: string; endTime?: string }) {
    return request<TradeStatistics>({
      url: '/transactions/statistics',
      method: 'get',
      params
    })
  },

  getList(params: TradeQuery) {
    return request<PageResult<TradeOrder>>({
      url: '/transactions',
      method: 'get',
      params
    })
  },

  getDetail(orderNo: string) {
    return request<TradeOrder>({
      url: `/transactions/${orderNo}`,
      method: 'get'
    })
  },

  getRefundList(params: RefundQuery) {
    return request<PageResult<RefundOrder>>({
      url: '/refunds',
      method: 'get',
      params
    })
  },

  getRefundDetail(refundNo: string) {
    return request<RefundOrder>({
      url: `/refunds/${refundNo}`,
      method: 'get'
    })
  },

  createRefund(orderNo: string, data: RefundRequest) {
    return request({
      url: `/transactions/${orderNo}/refund`,
      method: 'post',
      data
    })
  },

  retryRefund(refundNo: string) {
    return request({
      url: `/refunds/${refundNo}/retry`,
      method: 'post'
    })
  },

  exportOrders(params: TradeQuery) {
    return request<Blob>({
      url: '/transactions/export',
      method: 'get',
      params,
      responseType: 'blob'
    })
  }
}
