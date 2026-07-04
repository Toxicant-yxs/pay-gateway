import request from '@/utils/request'
import type { TradeOrder, TradeQuery, RefundOrder, RefundQuery } from '@/types/transaction'
import type { PageResult } from '@/types/api'

export const tradeApi = {
  getList(params: TradeQuery) {
    return request<PageResult<TradeOrder>>({
      url: '/trade/orders',
      method: 'get',
      params
    })
  },

  getDetail(id: number | string) {
    return request<TradeOrder>({
      url: `/trade/orders/${id}`,
      method: 'get'
    })
  }
}

export const refundApi = {
  getList(params: RefundQuery) {
    return request<PageResult<RefundOrder>>({
      url: '/refunds',
      method: 'get',
      params
    })
  },

  getDetail(id: number | string) {
    return request<RefundOrder>({
      url: `/refunds/${id}`,
      method: 'get'
    })
  }
}
