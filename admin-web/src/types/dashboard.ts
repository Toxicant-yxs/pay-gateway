export interface DashboardStatistics {
  todayAmount: number
  todayAmountGrowth: number
  todayCount: number
  todayCountGrowth: number
  successRate: number
  successRateChange: number
  avgLatency: number
  avgLatencyChange: number
}

export interface TrendData {
  xAxis?: string[]
  xaxis?: string[]
  successAmount: number[]
  successCount: number[]
  failCount: number[]
}

export interface ChannelStatusItem {
  channelId?: number | string
  channelCode: string
  channelName: string
  payTypes: string | string[]
  status: number | string
  avgSuccessRate?: number
  successRate?: number
  avgLatency: number
  dailyCount?: number
  qps?: number
  dailyAmount?: number
}

export interface AlertItem {
  id?: string
  alertId?: string
  type?: AlertType | string
  level: 'HIGH' | 'MEDIUM' | 'LOW' | 'high' | 'medium' | 'low' | string
  title: string
  content?: string
  message?: string
  createdAt?: string
  createdTime?: string
  time?: string
  read?: boolean
}

export type AlertType = 'CHANNEL_TIMEOUT' | 'RISK_BLOCK' | 'TRADE_WARNING' | 'RECON_DONE' | 'SYSTEM_NOTICE' | string

export interface RecentTrade {
  orderNo: string
  merchantNo?: string
  merchantName?: string
  channelCode?: string
  channel?: string
  amount: number
  payType?: string
  payMethod?: string
  status: number | string
  createdAt?: string
  createdTime?: string
  time?: string
  subject?: string
  clientIp?: string
  paidAt?: string
  actualAmount?: number
  fee?: number
}

export interface DistributionItem {
  name: string
  value: number
  amount: number
  percentage?: number
}
