export interface SystemConfigItem {
  id: string
  configKey: string
  configValue: string
  configGroup: string
  description?: string
}

export interface SystemConfigResponse {
  groups: { [key: string]: SystemConfigItem[] }
  list: SystemConfigItem[]
}

export interface SystemNotification {
  id: string
  title: string
  content: string
  category: string
  isRead: number
  createdAt: string
}

export interface OperationLog {
  id: string
  userId: string
  username: string
  operation: string
  method: string
  params?: string
  ip: string
  duration: number
  status: number
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
