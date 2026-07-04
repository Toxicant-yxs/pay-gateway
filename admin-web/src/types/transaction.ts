export interface TradeStatistics {
  totalCount: number
  totalAmount: number
  successCount: number
  successRate: number
}

export interface TradeOrder {
  id?: string
  orderNo: string
  merchantId?: string
  merchantNo?: string
  merchantName?: string
  channelCode?: ChannelCode | string
  channelName?: string
  channel?: string
  channelOrderNo?: string
  payType?: PayType | string
  payMethod?: string
  amount: number
  actualAmount?: number
  fee?: number
  currency?: string
  status: number | TradeStatus
  subject?: string
  clientIp?: string
  createdAt?: string
  createdTime?: string
  createTime?: string
  paidAt?: string
  payTime?: string
  expireTime?: string
  traceId?: string
  channelTradeNo?: string
  channelMsg?: string
  channelCode_resp?: string
}

export type TradeStatus = 'PENDING' | 'PAYING' | 'SUCCESS' | 'FAILED' | 'CLOSED' | 'REFUNDED' | 'PARTIAL_REFUNDED' | number
export type PayType = 'JSAPI' | 'NATIVE' | 'H5' | 'APP' | 'PC' | 'CREDIT_CARD' | 'CLOUD_FAST' | string
export type ChannelCode = 'WECHAT' | 'ALIPAY' | 'UNIONPAY' | 'VISA' | 'DCB' | string

export interface TradeQuery {
  orderNo?: string
  merchantId?: string
  merchantNo?: string
  merchantName?: string
  channelCode?: ChannelCode | string
  channel?: string
  status?: number | TradeStatus | ''
  payType?: PayType | string
  payMethod?: string
  startTime?: string
  endTime?: string
  minAmount?: number
  maxAmount?: number
  page?: number
  pageSize?: number
}

export interface RefundOrder {
  id: string
  refundNo: string
  orderNo: string
  merchantId: string
  merchantNo: string
  merchantName: string
  channelCode: ChannelCode
  refundAmount: number
  refundFee: number
  refundReason: string
  status: RefundStatus
  channelRefundNo?: string
  createdAt: string
  refundedAt?: string
}

export type RefundStatus = 'REFUNDING' | 'SUCCESS' | 'FAILED'

export interface RefundQuery {
  refundNo?: string
  orderNo?: string
  merchantName?: string
  status?: RefundStatus | ''
  startTime?: string
  endTime?: string
  page?: number
  pageSize?: number
}

export interface RefundRequest {
  orderNo: string
  refundAmount: number
  refundReason: string
}
