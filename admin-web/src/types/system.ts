export interface SystemConfig {
  basic: BasicConfig
  trade: TradeConfig
  notify: NotifyConfig
  security: SecurityConfig
}

export interface BasicConfig {
  systemName: string
  logo?: string
  defaultTimezone: string
  defaultCurrency: string
}

export interface TradeConfig {
  orderTimeout: number
  autoRefund: boolean
  callbackRetryTimes: number
}

export interface NotifyConfig {
  channels: NotifyChannel[]
  receivers: string[]
}

export type NotifyChannel = 'IN_APP' | 'EMAIL' | 'SMS'

export interface SecurityConfig {
  passwordMinLength: number
  passwordRequireMixed: boolean
  loginFailLockCount: number
  sessionTimeout: number
}

export interface SystemNotification {
  notifyId: string
  category: 'RISK' | 'TRANSACTION' | 'SYSTEM' | 'ALL'
  type: string
  title: string
  content: string
  read: boolean
  createdAt: string
  linkTo?: string
}

export interface OperationLog {
  logId: string
  userId: string
  username: string
  operation: string
  method: string
  params?: string
  ip: string
  status: 'success' | 'failed'
  costTime: number
  errorMsg?: string
  createdAt: string
}

export interface SearchResult {
  pages: PageSearchItem[]
  merchants: MerchantSearchItem[]
  orders: OrderSearchItem[]
}

export interface PageSearchItem {
  title: string
  path: string
  icon: string
}

export interface MerchantSearchItem {
  merchantId: string
  merchantName: string
  status: string
}

export interface OrderSearchItem {
  orderNo: string
  amount: number
  status: string
  merchantName: string
}
