import request from '@/utils/request'
import type {
  DashboardStatistics,
  TrendData,
  ChannelStatusItem,
  AlertItem,
  RecentTrade,
  DistributionItem
} from '@/types/dashboard'

export const dashboardApi = {
  getStatistics(params?: { startTime?: string; endTime?: string }) {
    return request<DashboardStatistics>({
      url: '/dashboard/stats',
      method: 'get',
      params
    })
  },

  getTrend(params: { type: '1h' | '24h' | '7d' }) {
    return request<TrendData>({
      url: '/dashboard/trend',
      method: 'get',
      params
    })
  },

  getChannelStatus() {
    return request<ChannelStatusItem[]>({
      url: '/dashboard/channels',
      method: 'get'
    })
  },

  getAlerts(params?: { page?: number; pageSize?: number }) {
    return request<{
      list: AlertItem[]
      total: number
    }>({
      url: '/dashboard/alerts',
      method: 'get',
      params
    })
  },

  getRecentTransactions(params?: { limit?: number }) {
    return request<RecentTrade[]>({
      url: '/dashboard/recent-transactions',
      method: 'get',
      params
    })
  },

  getAmountDistribution(params: { dimension: 'channel' | 'currency' }) {
    return request<DistributionItem[]>({
      url: '/dashboard/amount-distribution',
      method: 'get',
      params
    })
  }
}
