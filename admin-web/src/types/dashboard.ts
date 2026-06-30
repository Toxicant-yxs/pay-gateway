export interface DashboardStatistics {
  todayAmount: number
  todayAmountGrowth: number
  todayCount: number
  todayCountGrowth: number
  successRate: number
  successRateChange: number
  avgResponseTime: number
  avgResponseTimeChange: number
}

export interface TrendPoint {
  time: string
  successAmount: number
  successCount: number
  failCount: number
}

export interface ChannelStatusItem {
  channelId: string
  channelName: string
  channelCode: string
  payTypes: string[]
  status: 'NORMAL' | 'FLUCTUATING' | 'ABNORMAL' | 'DISABLED'
  successRate: number
  avgLatency: number
  qps: number
}

export interface AlertItem {
  alertId: string
  type: AlertType
  level: 'HIGH' | 'MEDIUM' | 'LOW'
  title: string
  content: string
  createdAt: string
  read: boolean
}

export type AlertType = 'CHANNEL_TIMEOUT' | 'RISK_BLOCK' | 'TRADE_WARNING' | 'RECON_DONE' | 'SYSTEM_NOTICE'

export interface RecentTrade {
  orderNo: string
  merchantName: string
  channelCode: string
  amount: number
  payType: string
  status: string
  createdAt: string
}

export interface DistributionItem {
  name: string
  value: number
  percentage: number
}
