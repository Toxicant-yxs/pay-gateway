import type { ChannelCode, PayType } from './transaction'

export interface ChannelInfo {
  channelId: string
  channelCode: ChannelCode
  channelName: string
  channelType: 1 | 2 | 3
  payTypes: PayType[]
  status: 0 | 1
  priority: number
  weight: number
  feeRates: Record<string, number>
  config: ChannelConfig
  avgSuccessRate: number
  avgLatency: number
  currentQps: number
  dailyAmount: number
  dailyCount: number
  appId?: string
  mchId?: string
  createdAt: string
  updatedAt: string
}

export interface ChannelConfig {
  appId: string
  mchId: string
  apiKey: string
  certPath?: string
  privateKey?: string
  publicKey?: string
  notifyUrl?: string
  singleLimit: number
  dailyLimit: number
  timeout: number
}

export interface ChannelQuery {
  channelName?: string
  channelCode?: ChannelCode | ''
  status?: 0 | 1 | ''
  page?: number
  pageSize?: number
}

export interface RouteRule {
  ruleId: string
  ruleName: string
  priority: number
  conditions: RouteCondition
  targets: RouteTarget[]
  status: 0 | 1
  description?: string
  createdAt: string
  effectStart?: string
  effectEnd?: string
}

export interface RouteCondition {
  minAmount?: number
  maxAmount?: number
  payTypes?: PayType[]
  merchantIds?: string[]
  timeStart?: string
  timeEnd?: string
}

export interface RouteTarget {
  targetId?: string
  ruleId?: string
  channelCode: ChannelCode
  channelName?: string
  weight: number
}

export interface ChannelStatus {
  status: 'NORMAL' | 'FLUCTUATING' | 'ABNORMAL' | 'DISABLED'
  label: string
}
