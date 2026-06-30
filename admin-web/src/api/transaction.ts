import request from '@/utils/request'
import type { TradeOrder, TradeQuery } from '@/types/transaction'
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
