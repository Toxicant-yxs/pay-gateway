import type { ChannelCode } from './transaction'

export interface ReconTask {
  id: string
  taskNo: string
  channelCode: string
  reconDate: string
  status: number
  totalCount: number
  successCount: number
  diffCount: number
  totalAmount: number
  successAmount: number
  diffAmount: number
  startTime?: string
  endTime?: string
  createdAt: string
  remark?: string
}

export interface ReconDetail {
  id: string
  taskId: string
  orderNo: string
  channelOrderNo: string
  orderAmount?: number
  channelAmount?: number
  diffType: 'SHORT' | 'EXTRA' | 'MISMATCH'
  diffAmount: number
  status: number
  handleNote?: string
  createdAt: string
}

export interface ReconStats {
  todayCount: number
  diffCount: number
  diffAmount: number
  pendingDiffCount: number
}

export interface ReconQuery {
  channelCode?: string
  status?: number | string
  startDate?: string
  endDate?: string
  reconDate?: string
  taskNo?: string
  page?: number
  pageSize?: number
}

export interface ReconReport {
  id: string
  reportName: string
  reportType: string
  channelCode: string
  startDate: string
  endDate: string
  totalCount: number
  totalAmount: number
  diffCount: number
  fileUrl?: string
  createdAt: string
}
