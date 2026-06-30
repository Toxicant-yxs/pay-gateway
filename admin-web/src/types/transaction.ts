export interface TradeStatistics {
  totalCount: number
  totalAmount: number
  successCount: number
  successRate: number
}

export interface TradeOrder {
  id: string
  orderNo: string
  merchantId: string
  merchantNo: string
  merchantName: string
  channelCode: ChannelCode
  channelName: string
  channelOrderNo?: string
  payType: PayType
  amount: number
  fee: number
  currency: string
  status: TradeStatus
  subject: string
  clientIp: string
  createdAt: string
  paidAt?: string
  expireTime: string
}

export type TradeStatus = 'PAYING' | 'SUCCESS' | 'FAILED' | 'CLOSED' | 'REFUNDED' | 'PARTIAL_REFUNDED'
export type PayType = 'JSAPI' | 'NATIVE' | 'H5' | 'APP' | 'PC' | 'CREDIT_CARD' | 'CLOUD_FAST'
export type ChannelCode = 'WECHAT' | 'ALIPAY' | 'UNIONPAY' | 'VISA' | 'DCB'

export interface TradeQuery {
  orderNo?: string
  merchantId?: string
  merchantName?: string
  channelCode?: ChannelCode | ''
  status?: TradeStatus | ''
  payType?: PayType | ''
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
